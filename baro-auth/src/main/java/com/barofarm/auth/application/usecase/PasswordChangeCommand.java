package com.barofarm.auth.application.usecase;

public record PasswordChangeCommand(String currentPassword, String newPassword) {
}
