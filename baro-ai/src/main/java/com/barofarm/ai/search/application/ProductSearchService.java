package com.barofarm.ai.search.application;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import com.barofarm.ai.search.application.dto.product.ProductAutoCompleteResponse;
import com.barofarm.ai.search.application.dto.product.ProductIndexRequest;
import com.barofarm.ai.search.application.dto.product.ProductSearchRequest;
import com.barofarm.ai.search.application.dto.product.ProductSearchResponse;
import com.barofarm.ai.search.domain.ProductAutocompleteDocument;
import com.barofarm.ai.search.domain.ProductDocument;
import com.barofarm.ai.search.infrastructure.elasticsearch.ProductAutocompleteRepository;
import com.barofarm.ai.search.infrastructure.elasticsearch.ProductSearchRepository;
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
public class ProductSearchService {
    private final ElasticsearchOperations operations;
    private final ProductSearchRepository repository;
    private final ProductAutocompleteRepository autocompleteRepository;

    // 상품 문서를 ES에 저장 (인덱싱), updatedAt은 현재 시각으로 자동 설정
    // Kafka Consumer에서 호출됨
    public ProductDocument indexProduct(ProductIndexRequest request) {
        ProductDocument doc =
            new ProductDocument(
                request.productId(),
                request.productName(),
                request.productCategory(),
                request.price(),
                request.status(),
                Instant.now());

        // 자동완성 인덱스에도 저장 (status 포함하여 필터링 가능하도록)
        ProductAutocompleteDocument autocompleteDoc =
            new ProductAutocompleteDocument(request.productId(), request.productName(), request.status());
        autocompleteRepository.save(autocompleteDoc);

        return repository.save(doc);
    }

    // 상품 삭제 (Kafka Consumer에서 호출됨)
    public void deleteProduct(UUID productId) {
        repository.deleteById(productId); // Document 삭제
        autocompleteRepository.deleteById(productId); // 자동완성 삭제
    }

    // 통합 검색을 위한 상품 검색 (키워드 하나만으로 검색)
    public CustomPage<ProductSearchResponse> searchProducts(String keyword, Pageable pageable) {

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

        SearchHits<ProductDocument> hits = operations.search(query, ProductDocument.class);

        List<ProductSearchResponse> items =
            hits.getSearchHits().stream()
                .map(h -> h.getContent())
                .map(d -> new ProductSearchResponse(
                    d.getProductId(),
                    d.getProductName(),
                    d.getProductCategory(),
                    d.getPrice()
                ))
                .toList();

        return CustomPage.of(hits.getTotalHits(), items, pageable);
    }

    // 상품 단독 검색 (필터링 조건 추가)
    public CustomPage<ProductSearchResponse> searchOnlyProducts(ProductSearchRequest request, Pageable pageable) {

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
                    applyCategoryFilter(b, request.categories());
                    applyPriceFilter(b, request.priceMin(), request.priceMax());

                    return b;
                }))
                .withSort(s -> s.score(sc -> sc.order(SortOrder.Desc)))
                .withSort(s -> s.field(f -> f.field("updatedAt").order(SortOrder.Desc)))
                .withPageable(pageable)
                .build();

        SearchHits<ProductDocument> hits = operations.search(query, ProductDocument.class);

        List<ProductSearchResponse> items =
            hits.getSearchHits().stream()
                .map(h -> h.getContent())
                .map(d -> new ProductSearchResponse(
                    d.getProductId(),
                    d.getProductName(),
                    d.getProductCategory(),
                    d.getPrice()
                ))
                .toList();

        return CustomPage.of(hits.getTotalHits(), items, pageable);
    }

    // 정확한 문구 검색
    private void applyExactMatch(BoolQuery.Builder b, String keyword) {
        b.should(m ->
            m.matchPhrase(mp ->
                mp.field("productName")
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
                mm.field("productName")
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
                mm.field("productName")
                  .query(keyword)
                  .fuzziness("AUTO") // ES가 자동으로 편집 거리 계산
                  .prefixLength(1)   // 앞 글자 1개는 정확히 일치해야 함
                  .boost(0.3f)       // 가장 낮은 가중치
            )
        );
    }

    // 상품 상태 필터
    private void applyStatusFilter(BoolQuery.Builder b) {
        b.filter(f ->
            f.terms(t ->
                t.field("status")
                 .terms(v -> v.value(
                     List.of(
                         FieldValue.of("ON_SALE"),
                         FieldValue.of("DISCOUNTED") // 판매 중, 할인 중인 상품만
                     )
                 ))
            )
        );
    }

    // 카테고리 필터
    private void applyCategoryFilter(BoolQuery.Builder b, List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            return; // 카테고리 필터 없음
        }

        b.filter(f ->
            f.terms(t ->
                t.field("productCategory")
                    .terms(v ->
                        v.value(
                            categories.stream()
                                .map(FieldValue::of)
                                .toList()
                        )
                    )
            )
        );
    }

    // 가격 필터
    private void applyPriceFilter(BoolQuery.Builder b, Long priceMin, Long priceMax) {
        if (priceMin == null && priceMax == null) {
            return; // 가격 필터 없음
        }

        b.filter(f ->
            f.range(r -> r.number(n -> {
                var numberRange = n.field("price");
                if (priceMin != null) {
                    numberRange = numberRange.gte(priceMin.doubleValue());
                }
                if (priceMax != null) {
                    numberRange = numberRange.lte(priceMax.doubleValue());
                }
                return numberRange;
            }))
        );
    }

    @Cacheable(value = "autocomplete", key = "#query")
    public List<ProductAutoCompleteResponse> autocomplete(String query, int size) {
        if (query == null || query.length() < 2) {
            return List.of(); // 최소 2글자 이상으로 제한
        }
        return autocompleteRepository.findByPrefix(query, size).stream()
            .map(document -> new ProductAutoCompleteResponse(document.getProductId(), document.getProductName()))
            .toList();
    }
}
