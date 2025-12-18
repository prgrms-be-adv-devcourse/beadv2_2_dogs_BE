package com.barofarm.order.payment.presentation.dto;

import com.barofarm.order.payment.application.dto.request.TossPaymentRefundCommand;

public record TossPaymentRefundRequest(
    String paymentKey,
    String cancelReason
) {
    public TossPaymentRefundCommand toCommand() {
        return new TossPaymentRefundCommand(
            paymentKey,
            cancelReason
        );
    }
}
