package com.barofarm.order.payment.application.dto.request;

public record TossPaymentConfirmCommand(
    String paymentKey,
    String orderId,
    Long amount
) {
}
