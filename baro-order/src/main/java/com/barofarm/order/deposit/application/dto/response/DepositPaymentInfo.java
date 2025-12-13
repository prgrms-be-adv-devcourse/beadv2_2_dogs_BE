package com.barofarm.order.deposit.application.dto.response;

import java.util.UUID;

public record DepositPaymentInfo(
    UUID orderId,
    long paidAmount,
    long remainingAmount
) {
    public static DepositPaymentInfo of(UUID orderId, long paidAmount, long remainingAmount) {
        return new DepositPaymentInfo(orderId, paidAmount, remainingAmount);
    }
}
