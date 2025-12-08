package com.barofarm.auth.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LogoutRequest(@Schema(description = "리프레시 토큰", example = "refresh-token") String refreshToken) {
}
