package com.barofarm.auth.presentation.dto.admin;

import com.barofarm.auth.domain.user.User;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminUserSummaryResponse(
//     @Schema(description = "사용자 ID", example = "8d0a4c8a-1111-2222-3333-444455556666")
    UUID userId,
//     @Schema(description = "이메일", example = "user@example.com")
    String email,
//     @Schema(description = "이름", example = "홍길동")
    String name,
//     @Schema(description = "전화번호", example = "010-1234-5678")
    String phone,
//     @Schema(description = "사용자 유형", example = "SELLER")
    User.UserType userType,
//     @Schema(description = "사용자 상태", example = "ACTIVE")
    User.UserState userState,
//     @Schema(description = "마지막 로그인 일시")
    LocalDateTime lastLoginAt,
//     @Schema(description = "생성 일시")
    LocalDateTime createdAt
) {

    public static AdminUserSummaryResponse from(User user) {
        return new AdminUserSummaryResponse(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getPhone(),
            user.getUserType(),
            user.getUserState(),
            user.getLastLoginAt(),
            user.getCreatedAt()
        );
    }
}
