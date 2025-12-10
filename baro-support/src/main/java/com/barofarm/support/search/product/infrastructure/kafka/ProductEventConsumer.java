package com.barofarm.support.search.product.infrastructure.kafka;

import com.barofarm.support.events.search.ProductEvent;
import com.barofarm.support.search.product.application.ProductSearchService;
import com.barofarm.support.search.product.application.dto.ProductIndexRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductEventConsumer {

    private final ProductSearchService productSearchService;

    // TODO: Producer 구현 후 테스트용 Controller 및 IndexRequest DTO 삭제 예정
    // - ProductIndexingController
    // - ProductIndexRequest

    // Product 모듈에서 상품 CRUD 시 product-events 토픽에 메세지 발행
    @KafkaListener(topics = "product-events", groupId = "search-service")
    public void onMessage(ProductEvent event) {
        ProductEvent.ProductEventData data = event.getData();
        switch (event.getType()) {
            case PRODUCT_CREATED, PRODUCT_UPDATED -> productSearchService.indexProduct(toRequest(data));
            case PRODUCT_DELETED -> productSearchService.deleteProduct(data.getProductId());
            default -> {
                // enum의 모든 케이스를 처리하므로 도달 불가능하지만 Checkstyle 요구사항 충족
            }
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
