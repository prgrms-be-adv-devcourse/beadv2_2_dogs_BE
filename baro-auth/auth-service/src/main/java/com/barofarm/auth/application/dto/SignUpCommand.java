package com.barofarm.auth.application.dto;

public record SignUpCommand(
    String email, String password, String name, String phone, boolean marketingConsent) {}
