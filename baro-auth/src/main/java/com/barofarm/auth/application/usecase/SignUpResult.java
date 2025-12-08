package com.barofarm.auth.application.usecase;

import io.swagger.v3.oas.annotations.media.Schema;

public record SignUpResult(@Schema(description = "New user ID", example = "1") Long userId,
        @Schema(description = "Email", example = "user@example.com") String email,
        @Schema(description = "Access token", example = "access-token") String accessToken,
        @Schema(description = "Refresh token", example = "refresh-token") String refreshToken) {
}
