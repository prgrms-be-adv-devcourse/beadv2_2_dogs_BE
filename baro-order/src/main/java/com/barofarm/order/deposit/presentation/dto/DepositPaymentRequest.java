package com.barofarm.order.deposit.presentation.dto;

import com.barofarm.order.deposit.application.dto.request.DepositPaymentCommand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record DepositPaymentRequest(
    @NotNull(message = "주문 ID는 필수입니다.")
    UUID orderId,

    @Positive(message = "결제 금액은 0보다 커야 합니다.")
    long amount
) {
    public DepositPaymentCommand toCommand() {
        return new DepositPaymentCommand(orderId, amount);
    }
}
