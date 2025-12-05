package com.barofarm.auth.application.dto;

public record SignUpRequest(
    String email, String password, String name, String phone, boolean marketingConsent) {}
