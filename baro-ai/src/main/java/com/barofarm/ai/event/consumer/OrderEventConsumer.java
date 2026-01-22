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
                case ORDER_CONFIRMED -> {
                    log.info(
                        "[ORDER_CONSUMER] Processing ORDER_CONFIRMED - User: {}, Order: {}",
                        event.userId(), data.orderId());

                    if (data.orderItems() != null) {
                        for (OrderLogEvent.OrderEventData.OrderItemData item : data.orderItems()) {
                            logWriteService.saveOrderEventLog(
                                event.userId(),
                                item.productId(),
                                item.productName(),
                                item.categoryName(),
                                "ORDER_CONFIRMED",
                                item.quantity(),
                                convertToInstant(event.ts())
                            );
                        }
                    }

                    log.info(
                        "[ORDER_CONSUMER] Saved ORDER_CONFIRMED logs - User: {}, Items: {}",
                        event.userId(),
                        data.orderItems() != null ? data.orderItems().size() : 0);
                    updateUserProfileAsync(event.userId());
                }
                case ORDER_CANCELLED -> {
                    log.info(
                        "[ORDER_CONSUMER] Processing ORDER_CANCELLED - User: {}, Order: {}",
                        event.userId(), data.orderId());

                    if (data.orderItems() != null) {
                        for (OrderLogEvent.OrderEventData.OrderItemData item : data.orderItems()) {
                            logWriteService.saveOrderEventLog(
                                event.userId(),
                                item.productId(),
                                item.productName(),
                                item.categoryName(),
                                "ORDER_CANCELLED",
                                item.quantity(),
                                convertToInstant(event.ts())
                            );
                        }
                    }

                    log.info(
                        "[ORDER_CONSUMER] Saved ORDER_CANCELLED logs - User: {}, Items: {}",
                        event.userId(),
                        data.orderItems() != null ? data.orderItems().size() : 0);
                    updateUserProfileAsync(event.userId());
                }
                default -> log.warn(
                    "[ORDER_CONSUMER] Unknown order event type - Type: {}, User: {}, Order: {}",
                    event.event(), event.userId(), data.orderId());
            }
        } catch (Exception e) {
            log.error(
                "[ORDER_CONSUMER] Failed to process order event - Type: {}, User: {}, Order: {}, Error: {}",
                event.event(), event.userId(), data.orderId(), e.getMessage(), e);
            throw e;
        }
    }

    private Instant convertToInstant(java.time.OffsetDateTime offsetDateTime) {
        return offsetDateTime.toInstant();
    }

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
        }
    }
}
