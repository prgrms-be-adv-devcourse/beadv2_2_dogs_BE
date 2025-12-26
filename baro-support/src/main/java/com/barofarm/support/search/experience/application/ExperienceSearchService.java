package com.barofarm.support.search.experience.application;

import static com.barofarm.support.search.util.KoreanChosungUtil.extract;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import com.barofarm.support.common.response.CustomPage;
import com.barofarm.support.search.experience.application.dto.ExperienceIndexRequest;
import com.barofarm.support.search.experience.application.dto.ExperienceSearchItem;
import com.barofarm.support.search.experience.domain.ExperienceDocument;
import com.barofarm.support.search.experience.infrastructure.elasticsearch.ExperienceSearchRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExperienceSearchService {
    private final ElasticsearchOperations operations;
    private final ExperienceSearchRepository repository;

    // 체험 문서를 ES에 저장 (인덱싱), updatedAt은 현재 시각으로 자동 설정
    // Kafka Consumer에서 호출됨
    public ExperienceDocument indexExperience(ExperienceIndexRequest request) {
        ExperienceDocument doc =
            new ExperienceDocument(
                request.experienceId(),
                request.experienceName(),
                extract(request.experienceName()),
                request.pricePerPerson(),
                request.capacity(),
                request.durationMinutes(),
                request.availableStartDate(),
                request.availableEndDate(),
                request.status(),
                Instant.now());
        return repository.save(doc);
    }

    // 체험 삭제 (Kafka Consumer에서 호출됨)
    public void deleteExperience(UUID experienceId) {
        repository.deleteById(experienceId);
    }

    public CustomPage<ExperienceSearchItem> searchExperiences(String keyword, Pageable pageable) {

        NativeQuery query =
            NativeQuery.builder()
                .withQuery(q -> q.bool(b -> {

                    // 키워드가 있는 경우에만 검색 조건을 추가
                    if (keyword != null && !keyword.isBlank()) {
                        applyExactMatch(b, keyword);
                        applyNormalMatch(b, keyword);
                        applyChosungMatch(b, keyword);
                        applyFuzzyMatch(b, keyword);
                        // should 조건 중 최소 하나는 만족해야 검색 결과에 포함
                        b.minimumShouldMatch("1");
                    }
                    applyStatusFilter(b);

                    return b;
                }))
                .withSort(s -> s.score(sc -> sc.order(SortOrder.Desc)))
                .withSort(s -> s.field(f -> f.field("updatedAt").order(SortOrder.Desc)))
                .withPageable(pageable)
                .build();

        SearchHits<ExperienceDocument> hits = operations.search(query, ExperienceDocument.class);

        List<ExperienceSearchItem> items =
            hits.getSearchHits().stream()
                .map(h -> h.getContent())
                .map(d -> new ExperienceSearchItem(
                    d.getExperienceId(),
                    d.getExperienceName(),
                    d.getPricePerPerson(),
                    d.getCapacity(),
                    d.getDurationMinutes()
                ))
                .toList();

        return CustomPage.of(hits.getTotalHits(), items, pageable);
    }

    // 정확한 문구 검색
    private void applyExactMatch(BoolQuery.Builder b, String keyword) {
        b.should(m ->
            m.matchPhrase(mp ->
                mp.field("experienceName")
                  .query(keyword)
                  .slop(1)      // 단어 사이에 최대 1개 단어 차이 허용
                  .boost(2.0f)  // 가장 높은 가중치
            )
        );
    }

    // 일반 키워드 검색 (OR 조건)
    private void applyNormalMatch(BoolQuery.Builder b, String keyword) {
        b.should(m ->
            m.match(mm ->
                mm.field("experienceName")
                  .query(keyword)
                  .operator(Operator.Or)
                  .boost(1.0f)
            )
        );
    }

    // 초성 검색
    private void applyChosungMatch(BoolQuery.Builder b, String keyword) {
        b.should(m ->
            m.prefix(p -> // prefix 비교만 수행
                p.field("experienceNameChosung")
                 .value(keyword)
                 .boost(0.5f)
            )
        );
    }

    // 오탈자 허용 검색
    private void applyFuzzyMatch(BoolQuery.Builder b, String keyword) {
        b.should(m ->
            m.match(mm ->
                mm.field("experienceName")
                  .query(keyword)
                  .fuzziness("AUTO") // ES가 자동으로 편집 거리 계산
                  .prefixLength(1)   // 앞 글자 1개는 정확히 일치해야 함
                  .boost(0.3f)       // 가장 낮은 가중치
            )
        );
    }

    // 체험 상태 필터
    private void applyStatusFilter(BoolQuery.Builder b) {
        b.filter(f ->
            f.term(t ->
                t.field("status")
                 .value("ON_SALE") // 판매 중인 상품만
            )
        );
    }
}
