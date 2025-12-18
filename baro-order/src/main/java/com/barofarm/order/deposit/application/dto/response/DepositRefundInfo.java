package com.barofarm.order.deposit.application.dto.response;

import java.util.UUID;

public record DepositRefundInfo(
    UUID orderId,
    long refundedAmount,
    long currentDepositAmount
) {
    public static DepositRefundInfo of(UUID orderId, long refundedAmount, long currentDepositAmount) {
        return new DepositRefundInfo(orderId, refundedAmount, currentDepositAmount);
    }
}
