package com.barofarm.order.payment.application.dto.response;

import com.barofarm.order.payment.domain.Payment;
import com.barofarm.order.payment.domain.PaymentStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record TossPaymentConfirmInfo(
    UUID id,
    String orderId,
    String paymentKey,
    Long amount,
    PaymentStatus status,
    String method,
    LocalDateTime requestedAt,
    LocalDateTime approvedAt,
    String failReason
) {
    public static TossPaymentConfirmInfo from(Payment payment) {
        return new TossPaymentConfirmInfo(
            payment.getId(),
            payment.getOrderId(),
            payment.getPaymentKey(),
            payment.getAmount(),
            payment.getStatus(),
            payment.getMethod(),
            payment.getRequestedAt(),
            payment.getApprovedAt(),
            payment.getFailReason()
        );
    }
}
