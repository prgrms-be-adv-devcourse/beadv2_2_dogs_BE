package com.barofarm.order.payment.application.dto.response;

import com.barofarm.order.payment.client.dto.TossPaymentResponse;

public record TossPaymentCancelInfo(
    String paymentKey,
    String orderId,
    String status,
    Long cancelAmount,
    String cancelReason
) {
    public static TossPaymentCancelInfo from(TossPaymentResponse response) {
        String reason = null;
        if (response.cancels() != null && !response.cancels().isEmpty()) {
            reason = response.cancels().get(0).cancelReason();
        }

        return new TossPaymentCancelInfo(
            response.paymentKey(),
            response.orderId(),
            response.status(),
            response.totalAmount(),
            reason
        );
    }
}
