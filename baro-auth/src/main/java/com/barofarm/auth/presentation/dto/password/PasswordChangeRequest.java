package com.barofarm.auth.presentation.dto.password;

import com.barofarm.auth.application.usecase.PasswordChangeCommand;
import io.swagger.v3.oas.annotations.media.Schema;

public record PasswordChangeRequest(
        @Schema(description = "현재 비밀번호", example = "OldP@ss1") String currentPassword,
        @Schema(description = "새 비밀번호", example = "N3wP@ss!") String newPassword) {

    public PasswordChangeCommand toServiceRequest() {
        return new PasswordChangeCommand(currentPassword, newPassword);
    }
}
