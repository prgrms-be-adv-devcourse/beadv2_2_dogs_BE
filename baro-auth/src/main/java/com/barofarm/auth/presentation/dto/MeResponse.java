package com.barofarm.auth.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record MeResponse(@Schema(description = "User ID", example = "8d0a4c8a-1111-2222-3333-444455556666") UUID userId,
        @Schema(description = "Email", example = "user@example.com") String email,
        @Schema(description = "Role", example = "USER") String role) {
}
