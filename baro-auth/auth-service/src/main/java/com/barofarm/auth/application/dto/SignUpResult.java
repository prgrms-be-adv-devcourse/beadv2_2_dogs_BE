package com.barofarm.auth.application.dto;

// 고민: 회원 가입후에 토큰을 바로 줄지 말지? -> 주는걸로
public record SignUpResult(Long userId, String email, String accessToken, String refreshToken) {}
