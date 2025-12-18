package com.barofarm.auth.application.usecase;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record LoginResult(
        @Schema(description = "User ID", example = "8d0a4c8a-1111-2222-3333-444455556666") UUID userId,
        @Schema(description = "Email", example = "user@example.com") String email,
        @Schema(description = "Access token", example = "access-token") String accessToken,
        @Schema(description = "Refresh token", example = "refresh-token") String refreshToken) {
}
