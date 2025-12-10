package com.barofarm.support.search.experience.application;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import com.barofarm.support.search.experience.application.dto.ExperienceIndexRequest;
import com.barofarm.support.search.experience.application.dto.ExperienceSearchResponse;
import com.barofarm.support.search.experience.domain.ExperienceDocument;
import com.barofarm.support.search.experience.infrastructure.elasticsearch.ExperienceSearchRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExperienceSearchService {
    private final ElasticsearchOperations operations;
    private final ExperienceSearchRepository repository;

    // 체험 문서를 ES에 저장 (인덱싱), updatedAt은 현재 시각으로 자동 설정
    // 추후 Kafka Consumer에서 호출 예정
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
        return repository.save(doc);
    }

    // 체험 삭제 (추후 Kafka Consumer에서 호출 예정)
    public void deleteExperience(UUID experienceId) {
        repository.deleteById(experienceId);
    }

    // 통합 검색용 체험 검색
    public ExperienceSearchResponse searchExperiences(String keyword, Pageable pageable) {
        NativeQuery query =
            NativeQuery.builder()
                .withQuery(
                    q ->
                        q.bool(
                            b -> {
                                if (keyword != null && !keyword.isBlank()) {
                                    // 1. 정확한 문구 매칭: 가장 높은 가중치
                                    b.should(
                                        m ->
                                            m.matchPhrase(
                                                mm ->
                                                    mm.field("experienceName")
                                                        .query(keyword)
                                                        .slop(1)
                                                        .boost(2.0f)));

                                    // 2. Or 기반으로 체험명 검색
                                    b.should(
                                        m ->
                                            m.match(
                                                mm ->
                                                    mm.field("experienceName")
                                                        .query(keyword)
                                                        .operator(Operator.Or)
                                                        .boost(1.0f)));

                                    b.minimumShouldMatch("1");
                                }

                                // 상태: ON_SALE만
                                b.filter(f -> f.term(t -> t.field("status").value("ON_SALE")));

                                return b;
                            }))
                .withSort(s -> s.score(sc -> sc.order(SortOrder.Desc)))
                .withSort(s -> s.field(f -> f.field("updatedAt").order(SortOrder.Desc)))
                .withPageable(pageable)
                .build();

        SearchHits<ExperienceDocument> hits = operations.search(query, ExperienceDocument.class);
        List<ExperienceDocument> items =
            hits.getSearchHits().stream().map(SearchHit::getContent).toList();

        return new ExperienceSearchResponse(hits.getTotalHits(), items);
    }
}
