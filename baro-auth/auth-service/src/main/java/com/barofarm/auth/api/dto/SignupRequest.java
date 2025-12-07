package com.barofarm.auth.api.dto;

import com.barofarm.auth.application.usecase.SignUpCommand;
import io.swagger.v3.oas.annotations.media.Schema;

public record SignupRequest(
    @Schema(description = "User email", example = "user@example.com") String email,
    @Schema(description = "Password", example = "P@ssw0rd!") String password,
    @Schema(description = "Name", example = "Jane Doe") String name,
    @Schema(description = "Phone number", example = "010-1234-5678") String phone,
    @Schema(description = "Marketing consent", example = "false") boolean marketingConsent) {
  public SignUpCommand toServiceRequest() {
    return new SignUpCommand(email, password, name, phone, marketingConsent);
  }
}
