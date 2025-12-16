package com.barofarm.auth.presentation.dto.user;

import com.barofarm.auth.domain.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record MeResponse(
        @Schema(description = "User ID", example = "8d0a4c8a-1111-2222-3333-444455556666") UUID userId,
        @Schema(description = "Email", example = "user@example.com") String email,
        @Schema(description = "Name", example = "홍길동") String name,
        @Schema(description = "Phone", example = "010-1111-2222") String phone,
        @Schema(description = "marketing Consent", example = "false") boolean marketingConsent,
        @Schema(description = "Role", example = "CUSTOMER") User.UserType role) {
}
