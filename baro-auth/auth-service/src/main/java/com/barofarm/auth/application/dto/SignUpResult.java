package com.barofarm.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SignUpResult(
    @Schema(description = "New user ID", example = "1") Long userId,
    @Schema(description = "Email", example = "user@example.com") String email,
    @Schema(description = "Access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,
    @Schema(description = "Refresh token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String refreshToken) {}
