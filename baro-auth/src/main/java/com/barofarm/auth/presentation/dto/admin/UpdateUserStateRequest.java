package com.barofarm.auth.presentation.dto.admin;

import com.barofarm.auth.domain.user.User;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStateRequest(
//     @Schema(description = "Account-level user state", example = "SUSPENDED")
    @NotNull User.UserState userState,
//     @Schema(description = "Optional reason for auditing", example = "manual block by admin")
    String reason
) {
}
