package com.barofarm.auth.application.usecase;

public record SignUpCommand(String email, String password, String name, String phone, boolean marketingConsent) {
}
