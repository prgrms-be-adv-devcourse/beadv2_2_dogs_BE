package com.barofarm.auth.presentation.dto.password;

import com.barofarm.auth.application.usecase.PasswordResetCommand;
import io.swagger.v3.oas.annotations.media.Schema;

public record PasswordResetConfirmRequest(
        @Schema(description = "이메일", example = "user@example.com") String email,
        @Schema(description = "이메일로 받은 인증 코드", example = "123456") String code,
        @Schema(description = "새 비밀번호", example = "N3wP@ss!") String newPassword) {

    public PasswordResetCommand toServiceRequest() {
        return new PasswordResetCommand(email, code, newPassword);
    }
}
