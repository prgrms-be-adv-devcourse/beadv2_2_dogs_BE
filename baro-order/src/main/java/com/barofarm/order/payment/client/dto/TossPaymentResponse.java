package com.barofarm.order.payment.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossPaymentResponse(
    String paymentKey,
    String orderId,
    @JsonProperty("totalAmount")
    Long totalAmount,
    String method,
    String status,
    OffsetDateTime requestedAt,
    OffsetDateTime approvedAt,
    List<Cancel> cancels
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Cancel(
        String transactionKey,
        String cancelReason,
        Long cancelAmount,
        OffsetDateTime canceledAt,
        String cancelStatus
    ) {}
}
