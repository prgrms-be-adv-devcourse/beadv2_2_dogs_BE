package com.barofarm.auth.application.usecase;

public record TokenResult(Long userId, String email, String accessToken, String refreshToken) {}
