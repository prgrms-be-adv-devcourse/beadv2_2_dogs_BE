package com.barofarm.order.payment.application.dto.request;

public record TossPaymentRefundCommand(
    String paymentKey,
    String cancelReason,
    Long cancelAmount
){
}
