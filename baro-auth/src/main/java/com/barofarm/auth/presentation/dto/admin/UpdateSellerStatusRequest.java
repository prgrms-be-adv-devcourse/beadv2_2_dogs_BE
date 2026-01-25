package com.barofarm.auth.presentation.dto.admin;

import com.barofarm.auth.domain.user.SellerStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateSellerStatusRequest(
//     @Schema(description = "판매자 승인 상태", example = "APPROVED")
    @NotNull SellerStatus sellerStatus,
//     @Schema(description = "처리 사유", example = "manual approval by admin")
    String reason
) {
}
