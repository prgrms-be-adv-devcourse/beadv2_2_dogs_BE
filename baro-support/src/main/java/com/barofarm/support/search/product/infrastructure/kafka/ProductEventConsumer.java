package com.barofarm.support.search.product.infrastructure.kafka;

import com.barofarm.support.event.ProductEvent;
import com.barofarm.support.search.product.application.ProductSearchService;
import com.barofarm.support.search.product.application.dto.ProductIndexRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventConsumer {

    private final ProductSearchService productSearchService;

    // Product 모듈에서 상품 CRUD 시 product-events 토픽에 메세지 발행
    @KafkaListener(topics = "product-events", groupId = "search-service")
    public void onMessage(ProductEvent event) {
        ProductEvent.ProductEventData data = event.getData();
        log.info("📨 [CONSUMER] Received product event - Type: {}, Product ID: {}, Name: {}, Category: {}, Price: {}",
                event.getType(), data.getProductId(), data.getProductName(),
                data.getProductCategory(), data.getPrice());

        try {
            switch (event.getType()) {
                case PRODUCT_CREATED -> {
                    log.info(
                        "🆕 [CONSUMER] Processing PRODUCT_CREATED - ID: {}, Name: {}, Category: {}, Price: {}",
                        data.getProductId(), data.getProductName(), data.getProductCategory(), data.getPrice());
                    productSearchService.indexProduct(toRequest(data));
                    log.info("✅ [CONSUMER] Successfully indexed product - ID: {}, Name: {}",
                        data.getProductId(), data.getProductName());
                }
                case PRODUCT_UPDATED -> {
                    log.info(
                        "🔄 [CONSUMER] Processing PRODUCT_UPDATED - ID: {}, Name: {}, Category: {}, Price: {}",
                        data.getProductId(), data.getProductName(), data.getProductCategory(), data.getPrice());
                    productSearchService.indexProduct(toRequest(data));
                    log.info("✅ [CONSUMER] Successfully updated product - ID: {}, Name: {}",
                        data.getProductId(), data.getProductName());
                }
                case PRODUCT_DELETED -> {
                    log.info("🗑️ [CONSUMER] Processing PRODUCT_DELETED event - Product ID: {}", data.getProductId());
                    productSearchService.deleteProduct(data.getProductId());
                    log.info("✅ [CONSUMER] Successfully deleted product - ID: {}", data.getProductId());
                }
                default -> {
                    log.warn("⚠️ [CONSUMER] Unknown event type received - Type: {}, Product ID: {}",
                        event.getType(), data.getProductId());
                }
            }
        } catch (Exception e) {
            log.error("❌ [CONSUMER] Failed to process product event - Type: {}, Product ID: {}, Name: {}, Error: {}",
                event.getType(), data.getProductId(), data.getProductName(), e.getMessage(), e);
            throw e; // 예외를 다시 던져서 Kafka가 재시도하도록 함
        }
    }

    private ProductIndexRequest toRequest(ProductEvent.ProductEventData data) {
        return new ProductIndexRequest(
            data.getProductId(),
            data.getProductName(),
            data.getProductCategory(),
            data.getPrice(),
            data.getStatus());
    }
}
