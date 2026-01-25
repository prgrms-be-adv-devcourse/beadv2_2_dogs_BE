package com.barofarm.ai.season.infrastructure.event;

import com.barofarm.ai.search.infrastructure.event.ProductEvent;
import com.barofarm.ai.season.application.SeasonalityDetectionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 상품 이벤트를 수신하여 제철 판단을 트리거하는 Consumer
 */
@Component("seasonalityProductEventConsumer")
@Slf4j
@RequiredArgsConstructor
public class ProductEventConsumer {

    private final SeasonalityDetectionService seasonalityService;

    /**
     * 상품 이벤트 수신 및 처리
     *
     * @param event 상품 이벤트
     */
    @KafkaListener(
        topics = "product-events",
        groupId = "ai-service-seasonality",
        containerFactory = "productEventListenerContainerFactory"
    )
    public void handleProductEvent(ProductEvent event) {
        log.debug("상품 이벤트 수신: type={}, productId={}",
            event.getType(), event.getData().getProductId());

        // 신규 상품 생성 시에만 제철 판단 수행
        if (event.getType() == ProductEvent.ProductEventType.PRODUCT_CREATED) {
            UUID productId = event.getData().getProductId();
            String productName = event.getData().getProductName();
            String productCategoryCode = event.getData().getProductCategoryCode();

            log.info("신규 상품 제철 판단 시작: productId={}, productName={}",
                productId, productName);

            // 비동기 처리 (LLM 호출은 시간이 걸릴 수 있음)
            seasonalityService.detectSeasonalityAsync(
                productId,
                productName,
                productCategoryCode
            );
        }
    }
}
