package com.barofarm.ai.event.model;

import com.barofarm.log.history.model.HistoryEventType;
import java.time.OffsetDateTime;
import java.util.UUID;

// Cart Kafka에서 전달되는 데이터 구조
public record CartLogEvent(
    HistoryEventType event,
    OffsetDateTime ts,
    UUID userId,
    CartEventData payload) {

    public record CartEventData(
        UUID cartId,
        UUID cartItemId,
        UUID productId,
        String productName,
        String categoryCode,
        Integer quantity
    ) { }
}
