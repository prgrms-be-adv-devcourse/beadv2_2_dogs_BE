package com.barofarm.ai.search.application;

import co.elastic.clients.elasticsearch._types.FieldValue;
import com.barofarm.ai.recommend.application.dto.ProductRecommendResponse;
import com.barofarm.ai.search.domain.ProductDocument;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

/**
 * 벡터 기반 상품 검색 서비스
 * 코사인 유사도를 이용한 상품 유사도 검색을 담당합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VectorProductSearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * 벡터 유사도를 기반으로 유사 상품을 검색합니다.
     * PersonalizedRecommendService와 SimilarProductService의 공통 로직을 추출했습니다.
     *
     * @param queryVector 검색할 벡터
     * @param topK 반환할 최대 상품 수
     * @param excludeProductIds 제외할 상품 ID 목록 (이미 주문/장바구니에 담은 상품들 또는 자기 자신 포함)
     * @param removeDuplicates 중복 제거 여부 (true일 경우 topK * 2개를 가져와 필터링)
     * @param targetCategoryCode 특정 카테고리 코드와 일치 시 보너스 (SimilarProductService용, null 가능)
     * @param categoryMatchBonus 카테고리 일치 시 보너스 점수 (0.0 ~ 1.0, 기본값 0.2)
     * @return 유사 상품 목록
     */
    public List<ProductRecommendResponse> findSimilarProductsByVector(
        float[] queryVector,
        int topK,
        List<UUID> excludeProductIds,
        boolean removeDuplicates,
        String targetCategoryCode,
        Double categoryMatchBonus
    ) {
        try {
            int fetchSize = removeDuplicates ? topK * 2 : topK;
            double bonus = categoryMatchBonus != null ? categoryMatchBonus : 0.2;
            boolean hasTargetCategory = targetCategoryCode != null;

            NativeQuery query = buildVectorSearchQuery(
                queryVector, excludeProductIds,
                targetCategoryCode, bonus, hasTargetCategory, fetchSize);

            SearchHits<ProductDocument> hits = elasticsearchOperations.search(query, ProductDocument.class);
            List<ProductRecommendResponse> results = processSearchResults(
                hits, topK, removeDuplicates, excludeProductIds, hasTargetCategory);

            return results;

        } catch (Exception e) {
            log.error("벡터 유사도 검색 실패: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 메도이드 알고리즘 등 후처리를 위해, 벡터 정보를 포함한 후보 상품 목록을 반환합니다.
     * 기존 findSimilarProductsByVector는 바로 DTO로 변환하지만,
     * 이 메서드는 ProductDocument 자체를 반환하여 벡터 연산에 활용할 수 있게 합니다.
     *
     * @param queryVector      검색할 벡터
     * @param candidateSize    가져올 후보 상품 개수 (예: topK * 3)
     * @param excludeProductIds 제외할 상품 ID 목록
     * @param targetCategoryCode  특정 카테고리 코드와 일치 시 보너스 (null 가능)
     * @param categoryMatchBonus 카테고리 일치 시 보너스 점수 (0.0 ~ 1.0, 기본값 0.2)
     * @return 벡터 정보를 포함한 후보 ProductDocument 목록
     */
    public List<ProductDocument> findSimilarProductDocumentsByVector(
        float[] queryVector,
        int candidateSize,
        List<UUID> excludeProductIds,
        String targetCategoryCode,
        Double categoryMatchBonus
    ) {
        if (candidateSize <= 0) {
            return List.of();
        }

        try {
            double bonus = categoryMatchBonus != null ? categoryMatchBonus : 0.2;
            boolean hasTargetCategory = targetCategoryCode != null;

            NativeQuery query = buildVectorSearchQuery(
                queryVector,
                excludeProductIds,
                targetCategoryCode,
                bonus,
                hasTargetCategory,
                candidateSize
            );

            SearchHits<ProductDocument> hits =
                elasticsearchOperations.search(query, ProductDocument.class);

            List<ProductDocument> results = new ArrayList<>();
            for (SearchHit<ProductDocument> hit : hits.getSearchHits()) {
                ProductDocument product = hit.getContent();
                if (product != null) {
                    results.add(product);
                }
                if (results.size() >= candidateSize) {
                    break;
                }
            }

            int excludedCount = excludeProductIds != null ? excludeProductIds.size() : 0;
            log.debug("벡터 유사도 후보 검색 결과: {}개 상품 발견 (요청 후보 수: {}, 제외 상품: {}, 카테고리 가중치: {})",
                results.size(), candidateSize, excludedCount, hasTargetCategory);

            return results;

        } catch (Exception e) {
            log.error("벡터 유사도 후보 검색 실패: {}", e.getMessage(), e);
            return List.of();
        }
    }

    // 벡터 검색 쿼리 빌드
    private NativeQuery buildVectorSearchQuery(
        float[] queryVector,
        List<UUID> excludeProductIds,
        String targetCategoryCode,
        double bonus,
        boolean hasTargetCategory,
        int fetchSize
    ) {
        String scriptSource = hasTargetCategory
            ? buildCategoryWeightedScript(bonus)
            : "cosineSimilarity(params.query_vector, 'vector') + 1.0";

        Map<String, Object> scriptParams = buildScriptParams(
            queryVector, targetCategoryCode, bonus, hasTargetCategory);

        return NativeQuery.builder()
            .withQuery(q -> q
                .scriptScore(ss -> ss
                    .query(q2 -> q2
                        .bool(b -> buildBoolQuery(b, excludeProductIds))
                    )
                    .script(s -> s
                        .inline(i -> i
                            .source(scriptSource)
                            .params(scriptParams.entrySet().stream()
                                .collect(java.util.stream.Collectors.toMap(
                                    Map.Entry::getKey,
                                    e -> co.elastic.clients.json.JsonData.of(e.getValue())
                                )))
                        )
                    )
                )
            )
            .withPageable(PageRequest.of(0, fetchSize))
            .build();
    }

    // Bool 쿼리 빌드
    private co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder buildBoolQuery(
        co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder b,
        List<UUID> excludeProductIds
    ) {
        // 제외할 상품 ID가 있으면 mustNot으로 필터링
        if (excludeProductIds != null && !excludeProductIds.isEmpty()) {
            b.mustNot(mn -> mn.ids(i -> i.values(
                excludeProductIds.stream()
                    .map(UUID::toString)
                    .toList()
            )));
        }
        b.filter(f -> f
            .terms(t -> t
                .field("status")
                .terms(v -> v.value(
                    List.of(
                        FieldValue.of("ON_SALE"),
                        FieldValue.of("DISCOUNTED")
                    )
                ))
            )
        );
        return b;
    }

    // 스크립트 파라미터 빌드
    private Map<String, Object> buildScriptParams(
        float[] queryVector,
        String targetCategoryCode,
        double bonus,
        boolean hasTargetCategory
    ) {
        Map<String, Object> scriptParams = new java.util.HashMap<>();
        scriptParams.put("query_vector", convertToDoubleList(queryVector));
        if (hasTargetCategory) {
            scriptParams.put("target_category_code", targetCategoryCode);
            scriptParams.put("category_bonus", bonus);
        }
        return scriptParams;
    }

    // 검색 결과 처리(ProductRecommendResponse 리스트로 변환)
    private List<ProductRecommendResponse> processSearchResults(
        SearchHits<ProductDocument> hits,
        int topK,
        boolean removeDuplicates,
        List<UUID> excludeProductIds,
        boolean hasTargetCategory
    ) {
        List<ProductRecommendResponse> results = new ArrayList<>();
        Set<UUID> seenProductIds = removeDuplicates ? new HashSet<>() : null;

        for (SearchHit<ProductDocument> hit : hits.getSearchHits()) {
            ProductDocument product = hit.getContent();
            UUID productId = product.getProductId();

            if (removeDuplicates && seenProductIds.contains(productId)) {
                log.debug("중복 상품 제거: productId={}, productName={}", productId, product.getProductName());
                continue;
            }

            if (results.size() >= topK) {
                break;
            }

            if (removeDuplicates) {
                seenProductIds.add(productId);
            }

            results.add(new ProductRecommendResponse(
                productId,
                product.getProductName(),
                product.getProductCategoryName(),
                product.getPrice()
            ));
        }

        int excludedCount = excludeProductIds != null ? excludeProductIds.size() : 0;
        log.debug("벡터 유사도 검색 결과: {}개 상품 발견 (제외 상품: {}, 중복 제거: {}, 카테고리 가중치: {})",
            results.size(), excludedCount, removeDuplicates, hasTargetCategory);
        return results;
    }

    // 카테고리 가중치를 포함한 Elasticsearch script 생성
    // targetCategoryCode와 일치하는 카테고리면 보너스 점수를 추가합니다.
    private String buildCategoryWeightedScript(double bonus) {
        return "double baseScore = cosineSimilarity(params.query_vector, 'vector') + 1.0; " +
               "double categoryBoost = 0.0; " +
               "if (doc['productCategoryCode'].size() > 0) { " +
               "  String categoryCode = doc['productCategoryCode'].value; " +
               "  if (categoryCode == params.target_category_code) { " +
               "    categoryBoost = params.category_bonus; " +
               "  } " +
               "} " +
               "return baseScore + categoryBoost;";
    }

    // float[]을 List<Double>로 변환
    // Elasticsearch JSON 파라미터에 사용하기 위함입니다.
    private List<Double> convertToDoubleList(float[] floatArray) {
        List<Double> doubleList = new java.util.ArrayList<>(floatArray.length);
        for (float f : floatArray) {
            doubleList.add((double) f);
        }
        return doubleList;
    }
}
