package com.barofarm.auth.api.dto;

public record VerifyCodeRequest(String email, String code) {}
