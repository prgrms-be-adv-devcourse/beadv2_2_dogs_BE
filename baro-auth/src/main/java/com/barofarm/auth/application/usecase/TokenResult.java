package com.barofarm.auth.application.usecase;

import java.util.UUID;

public record TokenResult(UUID userId, String email, String accessToken, String refreshToken) {
}
