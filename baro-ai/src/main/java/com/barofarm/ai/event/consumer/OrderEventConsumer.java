package com.barofarm.ai.event.consumer;

import com.barofarm.ai.embedding.application.UserProfileEmbeddingService;
import com.barofarm.ai.event.model.OrderLogEvent;
import com.barofarm.ai.log.application.LogWriteService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


/**
 * Order 이벤트 Kafka Consumer
 * 개인화 추천을 위한 주문 행동 로그 수집
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final LogWriteService logWriteService;
    private final UserProfileEmbeddingService userProfileEmbeddingService;

    @KafkaListener(
        topics = "order-events",
        groupId = "ai-service-order",
        containerFactory = "orderEventListenerContainerFactory"
    )
    public void onMessage(OrderLogEvent event) {
        OrderLogEvent.OrderEventData data = event.payload();

        log.info(
            "[ORDER_CONSUMER] Received order event - Type: {}, User ID: {}, Order ID: {}",
            event.event(), event.userId(), data.orderId());

        try {
            switch (event.event()) {
                case ORDER_CONFIRMED -> handleConfirmed(event, data);
                case ORDER_CANCELLED -> handleCancelled(event, data);
                default -> log.warn(
                    "[ORDER_CONSUMER] Unknown order event type received - Type: {}, User: {}, Order: {}",
                    event.event(), event.userId(), data.orderId());
            }
        } catch (Exception e) {
            log.error(
                "[ORDER_CONSUMER] Failed to process order event - Type: {}, User: {}, Order: {}, Error: {}",
                event.event(), event.userId(), data.orderId(), e.getMessage(), e);
            throw e;
        }
    }

    private void handleConfirmed(OrderLogEvent event, OrderLogEvent.OrderEventData data) {
        log.info(
            "[ORDER_CONSUMER] Processing ORDER_CONFIRMED - User: {}, Order: {}",
            event.userId(), data.orderId());

        // 주문 생성 시 각 상품별로 로그 저장
        if (data.orderItems() != null) {
            for (OrderLogEvent.OrderEventData.OrderItemData item : data.orderItems()) {
                logWriteService.saveOrderEventLog(
                    event.userId(),
                    item.productId(),
                    item.productName(),
                    item.categoryCode(),
                    "ORDER_CONFIRMED",
                    item.quantity(),
                    convertToInstant(event.ts())
                );
            }
        }

        log.info(
            "[ORDER_CONSUMER] Successfully saved order confirmed logs - User: {}, Items: {}",
            event.userId(), data.orderItems() != null ? data.orderItems().size() : 0);
        // 프로필 벡터 비동기 업데이트
        updateUserProfileAsync(event.userId());
    }

    private void handleCancelled(OrderLogEvent event, OrderLogEvent.OrderEventData data) {
        log.info(
            "[ORDER_CONSUMER] Processing ORDER_CANCELLED - User: {}, Order: {}",
            event.userId(), data.orderId());

        // 주문 취소 시 각 상품별로 로그 저장
        if (data.orderItems() != null) {
            for (OrderLogEvent.OrderEventData.OrderItemData item : data.orderItems()) {
                logWriteService.saveOrderEventLog(
                    event.userId(),
                    item.productId(),
                    item.productName(),
                    item.categoryCode(),
                    "ORDER_CANCELLED",
                    item.quantity(),
                    convertToInstant(event.ts())
                );
            }
        }

        log.info(
            "[ORDER_CONSUMER] Successfully saved order cancelled logs - User: {}, Items: {}",
            event.userId(), data.orderItems() != null ? data.orderItems().size() : 0);
        // 프로필 벡터 비동기 업데이트
        updateUserProfileAsync(event.userId());
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
            log.debug("[ORDER_CONSUMER] Updating user profile embedding for user: {}", userId);
            userProfileEmbeddingService.updateUserProfileEmbedding(userId);
            log.debug("[ORDER_CONSUMER] Successfully updated user profile embedding for user: {}", userId);
        } catch (Exception e) {
            log.warn(
                "[ORDER_CONSUMER] Failed to update user profile embedding for user: {}, error: {}",
                userId, e.getMessage());
            // 프로필 업데이트 실패는 이벤트 처리에 영향을 주지 않음
        }
    }
}
