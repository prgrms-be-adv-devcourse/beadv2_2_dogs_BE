package com.barofarm.support.review.presentation.dto;

import com.barofarm.support.review.application.dto.request.ReviewImageUpdateMode;
import com.barofarm.support.review.application.dto.request.ReviewUpdateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReviewUpdateRequest(
    @NotNull
    @Min(1) @Max(5)
    Integer rating,

    @NotNull
    ReviewVisibility reviewVisibility,

    @NotBlank
    String content,

    @NotNull
    @Schema(description = "이미지 변경 방식: KEEP(변경 없음), REPLACE(교체), CLEAR(전부 삭제)")
    ReviewImageUpdateMode imageUpdateMode
) {
    public ReviewUpdateCommand toCommand(UUID userId, UUID reviewId) {
        return new ReviewUpdateCommand(
            reviewId,
            userId,
            rating,
            reviewVisibility,
            content,
            imageUpdateMode
        );
    }
}
