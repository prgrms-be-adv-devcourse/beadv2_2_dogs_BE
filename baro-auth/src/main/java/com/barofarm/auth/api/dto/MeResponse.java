package com.barofarm.auth.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record MeResponse(@Schema(description = "User ID", example = "1") Long userId,
        @Schema(description = "Email", example = "user@example.com") String email,
        @Schema(description = "Role", example = "USER") String role) {
}
