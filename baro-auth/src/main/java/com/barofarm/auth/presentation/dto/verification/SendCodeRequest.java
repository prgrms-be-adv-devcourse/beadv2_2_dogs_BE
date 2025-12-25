package com.barofarm.auth.presentation.dto.verification;

import io.swagger.v3.oas.annotations.media.Schema;

public record SendCodeRequest(
        @Schema(description = "Email to send verification code", example = "user@example.com") String email) {
}
