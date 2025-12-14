package com.barofarm.order.payment.presentation.dto;

import com.barofarm.order.payment.application.dto.request.TossPaymentConfirmCommand;

public record TossPaymentConfirmRequest(
    String paymentKey,
    String orderId,
    Long amount
) {

    public TossPaymentConfirmCommand toCommand() {
        return new TossPaymentConfirmCommand(
            paymentKey,
            orderId,
            amount
        );
    }
}
