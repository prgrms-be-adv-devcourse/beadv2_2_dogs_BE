package com.barofarm.ai.search.application;

import com.barofarm.ai.embedding.application.ProductEmbeddingService;
import com.barofarm.ai.embedding.application.UserProfileEmbeddingService;
import com.barofarm.ai.search.application.dto.product.ProductAutoCompleteResponse;
import com.barofarm.ai.search.application.dto.product.ProductIndexRequest;
import com.barofarm.ai.search.domain.ProductAutocompleteDocument;
import com.barofarm.ai.search.domain.ProductDocument;
import com.barofarm.ai.search.infrastructure.elasticsearch.ProductAutocompleteRepository;
import com.barofarm.ai.search.infrastructure.elasticsearch.ProductSearchRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 상품 인덱싱 및 관련 서비스
 * Elasticsearch 상품 인덱스 관리, 자동완성, 사용자 프로필 업데이트를 담당합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductIndexService {

    private final ProductSearchRepository repository;
    private final ProductAutocompleteRepository autocompleteRepository;
    private final ProductEmbeddingService productEmbeddingService;
    private final UserProfileEmbeddingService userProfileEmbeddingService;

    /**
     * 상품 문서를 ES에 저장 (인덱싱), updatedAt은 현재 시각으로 자동 설정
     * Kafka Consumer에서 호출됨
     */
    public ProductDocument indexProduct(ProductIndexRequest request) {
        float[] vector = null;

        // 임베딩이 실패하더라도 인덱싱은 계속되어야 함.
        try {
            // 상품 이름을 기반으로 임베딩 생성
            vector = productEmbeddingService.embedProduct(request.productName());
        } catch (Exception e) {
            log.error("❌ Product embedding failed. productId=" + request.productId() + ", error=" + e.getMessage(), e);
        }

        ProductDocument doc =
            new ProductDocument(
                request.productId(),
                request.productName(),
                request.productCategoryId(),
                request.productCategoryCode(),
                request.productCategoryName(),
                request.price(),
                request.status(),
                Instant.now(),
                vector);

        // 자동완성 인덱스에도 저장 (status 포함하여 필터링 가능하도록)
        ProductAutocompleteDocument autocompleteDoc =
            new ProductAutocompleteDocument(request.productId(), request.productName(), request.status());
        autocompleteRepository.save(autocompleteDoc);

        return repository.save(doc);
    }

    /**
     * 상품 삭제 (Kafka Consumer에서 호출됨)
     */
    public void deleteProduct(UUID productId) {
        repository.deleteById(productId); // Document 삭제
        autocompleteRepository.deleteById(productId); // 자동완성 삭제
    }

    /**
     * 상품 자동완성 검색
     */
    @Cacheable(value = "autocomplete", key = "#query")
    public List<ProductAutoCompleteResponse> autocomplete(String query, int size) {
        if (query == null || query.length() < 2) {
            return List.of(); // 최소 2글자 이상으로 제한
        }
        return autocompleteRepository.findByPrefix(query, size).stream()
            .map(document -> new ProductAutoCompleteResponse(document.getProductId(), document.getProductName()))
            .toList();
    }

    /**
     * 사용자 프로필 벡터를 비동기로 업데이트합니다.
     * 검색 로그 저장 후 호출되며, 검색 성능에 영향을 주지 않도록 별도 스레드에서 실행됩니다.
     */
    @Async("profileUpdateExecutor")
    public void updateUserProfileAsync(UUID userId) {
        try {
            log.debug("🔄 [INDEX_SERVICE] Updating user profile embedding for user: {}", userId);
            userProfileEmbeddingService.updateUserProfileEmbedding(userId);
            log.debug("✅ [INDEX_SERVICE] Successfully updated user profile embedding for user: {}", userId);
        } catch (Exception e) {
            log.warn("⚠️ [INDEX_SERVICE] Failed to update user profile embedding for user: {}, error: {}",
                    userId, e.getMessage());
            // 프로필 업데이트 실패는 검색 결과에 영향을 주지 않음
        }
    }
}
