package com.barofarm.order.payment.application.dto.request;

public record TossPaymentCancelCommand(
    String paymentKey,
    String cancelReason,
    Long cancelAmount
){
}
