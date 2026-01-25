package com.barofarm.ai.event.model;

import com.barofarm.log.history.model.HistoryEventType;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

// Order Kafka에서 전달되는 데이터 구조
public record OrderLogEvent(
    HistoryEventType event,
    OffsetDateTime ts,
    UUID userId,
    OrderEventData payload) {

    public record OrderEventData(
        UUID orderId,
        List<OrderItemData> orderItems
    ) {
        public record OrderItemData(
            UUID productId,
            String productName,
            Integer quantity,
            String categoryCode
        ) { }
    }
}
