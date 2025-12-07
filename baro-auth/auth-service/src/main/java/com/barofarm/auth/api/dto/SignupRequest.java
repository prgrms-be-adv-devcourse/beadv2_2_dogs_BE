package com.barofarm.auth.api.dto;

import com.barofarm.auth.application.dto.SignUpCommand;

public record SignupRequest(
    String email, String password, String name, String phone, boolean marketingConsent) {
  public SignUpCommand toServiceRequest() {
    return new SignUpCommand(email, password, name, phone, marketingConsent);
  }
}
