package com.barofarm.support.search.product.infrastructure.kafka;

import com.barofarm.support.event.ProductEvent;
import com.barofarm.support.search.product.application.ProductSearchService;
import com.barofarm.support.search.product.application.dto.ProductIndexRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventConsumer {

    private final ProductSearchService productSearchService;

    // TODO: Producer 구현 후 테스트용 Controller 및 IndexRequest DTO 삭제 예정
    // - ProductIndexingController
    // - ProductIndexRequest

    // Product 모듈에서 상품 CRUD 시 product-events 토픽에 메세지 발행
    @KafkaListener(topics = "product-events", groupId = "search-service")
    public void onMessage(@Payload ProductEvent event,
                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                         @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                         @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("📨 [CONSUMER] Received product event from topic '{}', partition {}, offset {}: {}",
                topic, partition, offset, event);

        try {
            ProductEvent.ProductEventData data = event.getData();
            switch (event.getType()) {
                case PRODUCT_CREATED -> {
                    log.info("🆕 [CONSUMER] Processing PRODUCT_CREATED event for product ID: {}", data.getProductId());
                    productSearchService.indexProduct(toRequest(data));
                    log.info("✅ [CONSUMER] Successfully indexed product ID: {}", data.getProductId());
                }
                case PRODUCT_UPDATED -> {
                    log.info("🔄 [CONSUMER] Processing PRODUCT_UPDATED event for product ID: {}", data.getProductId());
                    productSearchService.indexProduct(toRequest(data));
                    log.info("✅ [CONSUMER] Successfully updated product ID: {}", data.getProductId());
                }
                case PRODUCT_DELETED -> {
                    log.info("🗑️ [CONSUMER] Processing PRODUCT_DELETED event for product ID: {}", data.getProductId());
                    productSearchService.deleteProduct(data.getProductId());
                    log.info("✅ [CONSUMER] Successfully deleted product ID: {}", data.getProductId());
                }
                default -> {
                    log.warn("⚠️ [CONSUMER] Unknown event type received: {}", event.getType());
                }
            }
        } catch (Exception e) {
            log.error("❌ [CONSUMER] Failed to process product event: {}", event, e);
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
