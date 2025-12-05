package com.barofarm.auth.api.dto;

import com.barofarm.auth.application.dto.SignUpRequest;

public record SignupRequest(
    String email, String password, String name, String phone, boolean marketingConsent) {
  public SignUpRequest toServiceRequest() {
    return new SignUpRequest(email, password, name, phone, marketingConsent);
  }
}
