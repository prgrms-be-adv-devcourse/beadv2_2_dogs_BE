package com.barofarm.buyer.product.application.event;

import com.barofarm.buyer.event.ProductEvent;
import com.barofarm.buyer.event.ProductEvent.ProductEventType;
import com.barofarm.buyer.product.domain.Product;
import com.barofarm.buyer.product.infrastructure.kafka.ProductEventProducer;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// 상품 도메인을 카프카 이벤트로 변환하고, 어떤 이벤트를 발행할지 결정
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventPublisher {

    private final ProductEventProducer producer;

    // 상품 생성 시 발행
    public void publishProductCreated(Product product) {
        log.info("📨 [EVENT_PUBLISHER] Building PRODUCT_CREATED event for product - ID: {}, Name: {}, Category: {}, Price: {}", 
            product.getId(), product.getProductName(), product.getProductCategory(), product.getPrice());
        ProductEvent event = buildEvent(ProductEventType.PRODUCT_CREATED, product);
        log.info("📨 [EVENT_PUBLISHER] Event built successfully - Type: {}, Product ID: {}", 
            event.getType(), event.getData().getProductId());
        producer.send(event);
    }

    // 상품 업데이트 시 발행
    public void publishProductUpdated(Product product) {
        producer.send(buildEvent(ProductEventType.PRODUCT_UPDATED, product));
    }

    // 상품 삭제 시 발행
    public void publishProductDeleted(Product product) {
        producer.send(buildEvent(ProductEventType.PRODUCT_DELETED, product));
    }

    private ProductEvent buildEvent(ProductEventType type, Product product) {
        return ProductEvent.builder()
            .type(type)
            .data(ProductEvent.ProductEventData.builder()
                .productId(product.getId())
                .productName(product.getProductName())
                .productCategory(product.getProductCategory().name()) // enum
                .price(product.getPrice())
                .status(product.getProductStatus().name()) // enum
                .updatedAt(Instant.now())
                .build()
            )
            .build();
    }
}
