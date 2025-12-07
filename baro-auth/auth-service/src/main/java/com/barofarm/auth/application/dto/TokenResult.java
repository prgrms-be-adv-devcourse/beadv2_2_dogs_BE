package com.barofarm.auth.application.dto;

public record TokenResult(Long userId, String email, String accessToken, String refreshToken) {}
