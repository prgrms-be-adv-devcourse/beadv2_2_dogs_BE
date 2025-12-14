package com.barofarm.order.deposit.presentation.dto;

import com.barofarm.order.deposit.application.dto.request.DepositChargeCreateCommand;
import jakarta.validation.constraints.Positive;

public record DepositChargeCreateRequest(
    @Positive(message = "충전 금액은 0보다 커야 합니다.")
    long amount
) {
    public DepositChargeCreateCommand toCommand(){
        return new DepositChargeCreateCommand(amount);
    }
}
