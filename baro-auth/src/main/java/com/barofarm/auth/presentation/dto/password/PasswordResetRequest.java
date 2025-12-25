package com.barofarm.auth.presentation.dto.password;

import io.swagger.v3.oas.annotations.media.Schema;

public record PasswordResetRequest(
        @Schema(description = "비밀번호 재설정 대상 이메일", example = "user@example.com") String email) {
}
