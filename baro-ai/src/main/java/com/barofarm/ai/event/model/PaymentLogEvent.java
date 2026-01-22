package com.barofarm.ai.event.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentLogEvent(
    HistoryEventType event,
    OffsetDateTime ts,
    UUID userId,
    PaymentEventData payload
) {
    public record PaymentEventData(
        UUID paymentId,
        UUID orderId,
        UUID userId,
        Long amount,
        String purpose
    ) { }
}

