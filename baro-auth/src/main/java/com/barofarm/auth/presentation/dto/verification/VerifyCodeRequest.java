package com.barofarm.auth.presentation.dto.verification;

import io.swagger.v3.oas.annotations.media.Schema;

public record VerifyCodeRequest(@Schema(description = "Email", example = "user@example.com") String email,
        @Schema(description = "Verification code", example = "123456") String code) {
}
