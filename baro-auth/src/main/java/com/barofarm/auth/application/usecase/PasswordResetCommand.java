package com.barofarm.auth.application.usecase;

public record PasswordResetCommand(String email, String code, String newPassword) {
}
