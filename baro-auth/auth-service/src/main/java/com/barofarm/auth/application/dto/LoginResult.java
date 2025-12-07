package com.barofarm.auth.application.dto;

public record LoginResult(Long userId, String email, String accessToken, String refreshToken) {}
