package com.barofarm.auth.presentation.dto.user;

import com.barofarm.auth.application.usecase.WithdrawCommand;
// import io.swagger.v3.oas.annotations.media.Schema;

public record WithdrawRequest(
//         @Schema(description = "Optional reason for withdrawal", example = "USER_REQUEST")
        String reason
) {
    public WithdrawCommand toServiceRequest() {
        return new WithdrawCommand(reason);
    }
}
