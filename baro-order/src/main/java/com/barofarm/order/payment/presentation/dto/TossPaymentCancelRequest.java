package com.barofarm.order.payment.presentation.dto;

import com.barofarm.order.payment.application.dto.request.TossPaymentCancelCommand;

public record TossPaymentCancelRequest(
    String paymentKey,
    String cancelReason,
    Long cancelAmount
) {
    public TossPaymentCancelCommand toCommand() {
        return new TossPaymentCancelCommand(
            paymentKey,
            cancelReason,
            cancelAmount
        );
    }
}
