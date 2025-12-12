package com.barofarm.order.payment.application.dto.response;

import com.barofarm.order.payment.client.dto.TossPaymentResponse;

public record TossPaymentRefundInfo(
    String paymentKey,
    String orderId,
    String status,
    Long cancelAmount,
    String cancelReason
) {
    public static TossPaymentRefundInfo from(TossPaymentResponse response) {
        String reason = null;
        if (response.cancels() != null && !response.cancels().isEmpty()) {
            reason = response.cancels().get(0).cancelReason();
        }

        return new TossPaymentRefundInfo(
            response.paymentKey(),
            response.orderId(),
            response.status(),
            response.totalAmount(),
            reason
        );
    }
}
