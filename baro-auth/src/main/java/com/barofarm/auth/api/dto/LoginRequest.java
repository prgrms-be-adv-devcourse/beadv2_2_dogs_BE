package com.barofarm.auth.api.dto;

import com.barofarm.auth.application.usecase.LoginCommand;
import io.swagger.v3.oas.annotations.media.Schema;

public record LoginRequest(@Schema(description = "User email", example = "user@example.com") String email,
        @Schema(description = "Password", example = "P@ssw0rd!") String password) {

    public LoginCommand toServiceRequest() {
        return new LoginCommand(email, password);
    }
}
