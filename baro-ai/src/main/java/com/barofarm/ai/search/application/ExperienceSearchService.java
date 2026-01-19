package com.barofarm.ai.search.application;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import com.barofarm.ai.search.application.dto.experience.ExperienceAutoCompleteResponse;
import com.barofarm.ai.search.application.dto.experience.ExperienceIndexRequest;
import com.barofarm.ai.search.application.dto.experience.ExperienceSearchRequest;
import com.barofarm.ai.search.application.dto.experience.ExperienceSearchResponse;
import com.barofarm.ai.search.domain.ExperienceAutocompleteDocument;
import com.barofarm.ai.search.domain.ExperienceDocument;
import com.barofarm.ai.search.infrastructure.elasticsearch.ExperienceAutocompleteRepository;
import com.barofarm.ai.search.infrastructure.elasticsearch.ExperienceSearchRepository;
import com.barofarm.dto.CustomPage;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
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
    private final ExperienceAutocompleteRepository autocompleteRepository;

    // 체험 문서를 ES에 저장 (인덱싱), updatedAt은 현재 시각으로 자동 설정
    // Kafka Consumer에서 호출됨
    public ExperienceDocument indexExperience(ExperienceIndexRequest request) {
        ExperienceDocument doc =
            new ExperienceDocument(
                request.experienceId(),
                request.experienceName(),
                request.pricePerPerson(),
                request.capacity(),
                request.durationMinutes(),
                request.availableStartDate(),
                request.availableEndDate(),
                request.status(),
                Instant.now());

        // 자동완성 인덱스에도 저장 (status 포함하여 필터링 가능하도록)
        ExperienceAutocompleteDocument autocompleteDoc =
            new ExperienceAutocompleteDocument(request.experienceId(), request.experienceName(), request.status());
        autocompleteRepository.save(autocompleteDoc);

        return repository.save(doc);
    }

    // 체험 삭제 (Kafka Consumer에서 호출됨)
    public void deleteExperience(UUID experienceId) {
        repository.deleteById(experienceId); // Document 삭제
        autocompleteRepository.deleteById(experienceId); // 자동완성 삭제
    }

    // 통합 검색을 위한 체험 검색 (키워드 하나만으로 검색)
    public CustomPage<ExperienceSearchResponse> searchExperiences(String keyword, Pageable pageable) {

        NativeQuery query =
            NativeQuery.builder()
                .withQuery(q -> q.bool(b -> {

                    // 키워드가 있는 경우에만 검색 조건을 추가
                    if (keyword != null && !keyword.isBlank()) {
                        applyExactMatch(b, keyword);
                        applyNormalMatch(b, keyword);

                        // 3글자 이상인 경우에만 오탈자 검색 허용
                        if (keyword.length() >= 3) {
                            applyFuzzyMatch(b, keyword);
                        }

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

        List<ExperienceSearchResponse> items =
            hits.getSearchHits().stream()
                .map(h -> h.getContent())
                .map(d -> new ExperienceSearchResponse(
                    d.getExperienceId(),
                    d.getExperienceName(),
                    d.getPricePerPerson(),
                    d.getCapacity(),
                    d.getDurationMinutes()
                ))
                .toList();

        return CustomPage.of(hits.getTotalHits(), items, pageable);
    }

    // 체험 단독 검색 (필터링 조건 추가)
    public CustomPage<ExperienceSearchResponse> searchOnlyExperiences(
        ExperienceSearchRequest request, Pageable pageable) {

        NativeQuery query =
            NativeQuery.builder()
                .withQuery(q -> q.bool(b -> {

                    // 키워드가 있는 경우에만 검색 조건을 추가
                    if (request.keyword() != null && !request.keyword().isBlank()) {
                        String keyword = request.keyword();

                        applyExactMatch(b, keyword);
                        applyNormalMatch(b, keyword);

                        // 3글자 이상인 경우에만 오탈자 검색 허용
                        if (keyword.length() >= 3) {
                            applyFuzzyMatch(b, keyword);
                        }

                        // should 조건 중 최소 하나는 만족해야 검색 결과에 포함
                        b.minimumShouldMatch("1");
                    }
                    applyStatusFilter(b);
                    applyCapacityFilter(b, request.capacityMin(), request.capacityMax());
                    applyDurationFilter(b, request.durationMin(), request.durationMax());
                    applyPricePerPersonFilter(b, request.pricePerPersonMin(), request.pricePerPersonMax());

                    return b;
                }))
                .withSort(s -> s.score(sc -> sc.order(SortOrder.Desc)))
                .withSort(s -> s.field(f -> f.field("updatedAt").order(SortOrder.Desc)))
                .withPageable(pageable)
                .build();

        SearchHits<ExperienceDocument> hits = operations.search(query, ExperienceDocument.class);

        List<ExperienceSearchResponse> items =
            hits.getSearchHits().stream()
                .map(h -> h.getContent())
                .map(d -> new ExperienceSearchResponse(
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

    // 1인당 가격 필터
    private void applyPricePerPersonFilter(BoolQuery.Builder b, Long min, Long max) {
        if (min == null && max == null) {
            return;
        }

        b.filter(f ->
            f.range(r -> r.number(n -> {
                var range = n.field("pricePerPerson");
                if (min != null) {
                    range = range.gte(min.doubleValue());
                }
                if (max != null) {
                    range = range.lte(max.doubleValue());
                }
                return range;
            }))
        );
    }

    // 수용 인원 필터
    private void applyCapacityFilter(BoolQuery.Builder b, Integer min, Integer max) {
        if (min == null && max == null) {
            return;
        }

        b.filter(f ->
            f.range(r -> r.number(n -> {
                var range = n.field("capacity");
                if (min != null) {
                    range = range.gte(min.doubleValue());
                }
                if (max != null) {
                    range = range.lte(max.doubleValue());
                }
                return range;
            }))
        );
    }

    // 소요 시간 필터 (분 단위)
    private void applyDurationFilter(BoolQuery.Builder b, Integer min, Integer max) {
        if (min == null && max == null) {
            return;
        }

        b.filter(f ->
            f.range(r -> r.number(n -> {
                var range = n.field("duration");
                if (min != null) {
                    range = range.gte(min.doubleValue());
                }
                if (max != null) {
                    range = range.lte(max.doubleValue());
                }
                return range;
            }))
        );
    }

    @Cacheable(value = "autocomplete", key = "#query")
    public List<ExperienceAutoCompleteResponse> autocomplete(String query, int size) {
        if (query == null || query.length() < 2) {
            return List.of(); // 최소 2글자 이상으로 제한
        }
        return autocompleteRepository.findByPrefix(query, size).stream()
            .map(document -> new ExperienceAutoCompleteResponse(
                document.getExperienceId(),
                document.getExperienceName()))
            .toList();
    }
}
