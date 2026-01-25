package com.barofarm.ai.event.consumer;

import com.barofarm.ai.embedding.application.UserProfileEmbeddingService;
import com.barofarm.ai.event.model.CartLogEvent;
import com.barofarm.ai.log.application.LogWriteService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Cart 이벤트 Kafka Consumer
 * 개인화 추천을 위한 장바구니 행동 로그 수집
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CartEventConsumer {

    private final LogWriteService logWriteService;
    private final UserProfileEmbeddingService userProfileEmbeddingService;

    @KafkaListener(
        topics = "cart-events",
        groupId = "ai-service-cart",
        containerFactory = "cartEventListenerContainerFactory"
    )
    public void onMessage(CartLogEvent event) {
        CartLogEvent.CartEventData data = event.payload();

        log.info("[CART_CONSUMER] Received cart event - Type: {}, User ID: {}, Product: {}, Quantity: {}",
            event.event(), event.userId(), data.productName(), data.quantity());

        try {
            switch (event.event()) {
                case CART_ITEM_ADDED -> {
                    log.info("[CART_CONSUMER] Processing CART_ITEM_ADDED - User: {}, Product: {}, Qty: {}",
                        event.userId(), data.productName(), data.quantity());
                    logWriteService.saveCartEventLog(
                        event.userId(),
                        data.productId(),
                        data.productName(),
                        data.categoryCode(),
                        "ADD",
                        data.quantity(),
                        convertToInstant(event.ts())
                    );
                    log.info("[CART_CONSUMER] Successfully saved cart add event - User: {}, Product: {}",
                        event.userId(), data.productName());
                    // 프로필 벡터 비동기 업데이트
                    updateUserProfileAsync(event.userId());
                }
                case CART_ITEM_REMOVED -> {
                    log.info("[CART_CONSUMER] Processing CART_ITEM_REMOVED - User: {}, Product: {}, Qty: {}",
                        event.userId(), data.productName(), data.quantity());
                    logWriteService.saveCartEventLog(
                        event.userId(),
                        data.productId(),
                        data.productName(),
                        data.categoryCode(),
                        "REMOVE",
                        data.quantity(),
                        convertToInstant(event.ts())
                    );
                    log.info("[CART_CONSUMER] Successfully saved cart remove event - User: {}, Product: {}",
                        event.userId(), data.productName());
                    // 프로필 벡터 비동기 업데이트
                    updateUserProfileAsync(event.userId());
                }
                case CART_QUANTITY_UPDATED -> {
                    log.info("[CART_CONSUMER] Processing CART_QUANTITY_UPDATED - User: {}, Product: {}, Qty: {}",
                        event.userId(), data.productName(), data.quantity());
                    logWriteService.saveCartEventLog(
                        event.userId(),
                        data.productId(),
                        data.productName(),
                        data.categoryCode(),
                        "UPDATE",
                        data.quantity(),
                        convertToInstant(event.ts())
                    );
                    log.info("[CART_CONSUMER] Successfully saved cart update event - User: {}, Product: {}",
                        event.userId(), data.productName());
                    // 프로필 벡터 비동기 업데이트
                    updateUserProfileAsync(event.userId());
                }
                default ->
                    log.warn("[CART_CONSUMER] Unknown cart event type received - Type: {}, User: {}, Product: {}",
                        event.event(), event.userId(), data.productName());
            }
        } catch (Exception e) {
            log.error("[CART_CONSUMER] Failed to process cart event - Type: {}, User: {}, Product: {}, Error: {}",
                event.event(), event.userId(), data.productName(), e.getMessage(), e);
            throw e; // 예외를 다시 던져서 Kafka가 재시도하도록 함
        }
    }

    private Instant convertToInstant(java.time.OffsetDateTime offsetDateTime) {
        return offsetDateTime.toInstant();
    }

    /**
     * 사용자 프로필 벡터를 비동기로 업데이트합니다.
     * 이벤트 처리 속도에 영향을 주지 않도록 별도 스레드에서 실행됩니다.
     */
    @Async("profileUpdateExecutor")
    public void updateUserProfileAsync(java.util.UUID userId) {
        try {
            log.debug("[CART_CONSUMER] Updating user profile embedding for user: {}", userId);
            userProfileEmbeddingService.updateUserProfileEmbedding(userId);
            log.debug("[CART_CONSUMER] Successfully updated user profile embedding for user: {}", userId);
        } catch (Exception e) {
            log.warn("[CART_CONSUMER] Failed to update user profile embedding for user: {}, error: {}",
                userId, e.getMessage());
            // 프로필 업데이트 실패는 이벤트 처리에 영향을 주지 않음
        }
    }
}
