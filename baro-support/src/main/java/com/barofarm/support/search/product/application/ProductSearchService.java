package com.barofarm.support.search.product.application;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import com.barofarm.support.search.product.application.dto.ProductIndexRequest;
import com.barofarm.support.search.product.application.dto.ProductSearchResponse;
import com.barofarm.support.search.product.domain.ProductDocument;
import com.barofarm.support.search.product.infrastructure.elasticsearch.ProductSearchRepository;
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
public class ProductSearchService {
    private final ElasticsearchOperations operations;
    private final ProductSearchRepository repository;

    // 상품 문서를 ES에 저장 (인덱싱), updatedAt은 현재 시각으로 자동 설정
    // 추후 Kafka Consumer에서 호출 예정
    public ProductDocument indexProduct(ProductIndexRequest request) {
        ProductDocument doc =
            new ProductDocument(
                request.productId(),
                request.productName(),
                request.productCategory(),
                request.price(),
                request.status(),
                Instant.now());
        return repository.save(doc);
    }

    // 상품 삭제 (추후 Kafka Consumer에서 호출 예정)
    public void deleteProduct(UUID productId) {
        repository.deleteById(productId);
    }

    // 통합 검색용 상품 검색
    public ProductSearchResponse searchProducts(String keyword, Pageable pageable) {
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
                                                    mm.field("productName")
                                                        .query(keyword)
                                                        .slop(1) // 단어 사이에 1개의 다른 단어 허용 (고당도 유기농 토마토도 OK)
                                                        .boost(2.0f)));

                                    // 2. Or 기반으로 상품명 검색
                                    b.should(
                                        m ->
                                            m.match(
                                                mm ->
                                                    mm.field("productName")
                                                        .query(keyword)
                                                        .operator(
                                                            Operator.Or) // "고당도 토마토" -> "고당도" or "토마토"
                                                        .boost(1.0f)));

                                    b.minimumShouldMatch("1"); // 최소 1개의 should 조건이 맞아야 함.
                                }

                                // 상태: ON_SALE, DISCOUNTED만
                                b.filter(f -> f.terms(t -> t
                                    .field("status")
                                    .terms(v -> v.value(
                                        List.of(
                                            FieldValue.of("ON_SALE"),
                                            FieldValue.of("DISCOUNTED")
                                        )
                                    ))
                                ));

                                return b;
                            }))
                .withSort(s -> s.score(sc -> sc.order(SortOrder.Desc))) // 점수순 정렬
                .withSort(s -> s.field(f -> f.field("updatedAt").order(SortOrder.Desc))) // 최신순 정렬
                .withPageable(pageable)
                .build();

        SearchHits<ProductDocument> hits = operations.search(query, ProductDocument.class);
        List<ProductDocument> items = hits.getSearchHits().stream().map(SearchHit::getContent).toList();

        return new ProductSearchResponse(hits.getTotalHits(), items);
    }
}
