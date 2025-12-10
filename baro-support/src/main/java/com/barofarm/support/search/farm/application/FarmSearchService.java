package com.barofarm.support.search.farm.application;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import com.barofarm.support.search.farm.application.dto.FarmIndexRequest;
import com.barofarm.support.search.farm.application.dto.FarmSearchResponse;
import com.barofarm.support.search.farm.domain.FarmDocument;
import com.barofarm.support.search.farm.infrastructure.elasticsearch.FarmSearchRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

@Service
public class FarmSearchService {
    private final ElasticsearchOperations operations;
    private final FarmSearchRepository repository;

    public FarmSearchService(ElasticsearchOperations operations, FarmSearchRepository repository) {
        this.operations = operations;
        this.repository = repository;
    }

    // 농장 문서를 ES에 저장 (인덱싱), updatedAt은 현재 시각으로 자동 설정
    // 추후 Kafka Consumer에서 호출 예정
    public FarmDocument indexFarm(FarmIndexRequest request) {
        FarmDocument doc =
            new FarmDocument(
                request.farmId(), request.farmName(), request.farmAddress(), request.status(), Instant.now());
        return repository.save(doc);
    }

    // 농장 삭제 (추후 Kafka Consumer에서 호출 예정)
    public void deleteFarm(UUID farmId) {
        repository.deleteById(farmId);
    }

    // 통합 검색용 농장 검색
    public FarmSearchResponse searchFarms(String keyword, Pageable pageable) {
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
                                                    mm.field("farmName")
                                                        .query(keyword)
                                                        .slop(1)
                                                        .boost(2.0f)));

                                    // 2. Or 기반으로 농장명 검색
                                    b.should(
                                        m ->
                                            m.match(
                                                mm ->
                                                    mm.field("farmName")
                                                        .query(keyword)
                                                        .operator(Operator.Or)
                                                        .boost(1.0f)));

                                    b.minimumShouldMatch("1");
                                }

                                // 상태: ACTIVE만
                                b.filter(f -> f.term(t -> t.field("status").value("ACTIVE")));

                                return b;
                            }))
                .withSort(s -> s.score(sc -> sc.order(SortOrder.Desc)))
                .withSort(s -> s.field(f -> f.field("updatedAt").order(SortOrder.Desc)))
                .withPageable(pageable)
                .build();

        SearchHits<FarmDocument> hits = operations.search(query, FarmDocument.class);
        List<FarmDocument> items = hits.getSearchHits().stream().map(SearchHit::getContent).toList();

        return new FarmSearchResponse(hits.getTotalHits(), items);
    }
}
