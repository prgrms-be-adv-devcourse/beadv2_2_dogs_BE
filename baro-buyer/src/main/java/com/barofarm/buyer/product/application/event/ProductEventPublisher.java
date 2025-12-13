package com.barofarm.buyer.product.application.event;

import com.barofarm.buyer.event.ProductEvent;
import com.barofarm.buyer.event.ProductEvent.ProductEventType;
import com.barofarm.buyer.product.domain.Product;
import com.barofarm.buyer.product.infrastructure.kafka.ProductEventProducer;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 상품 도메인을 카프카 이벤트로 변환하고, 어떤 이벤트를 발행할지 결정
@Component
@RequiredArgsConstructor
public class ProductEventPublisher {

    private final ProductEventProducer producer;

    // 상품 생성 시 발행
    public void publishProductCreated(Product product) {
        producer.send(buildEvent(ProductEventType.PRODUCT_CREATED, product));
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
