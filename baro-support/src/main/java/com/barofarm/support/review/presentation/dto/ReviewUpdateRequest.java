package com.barofarm.support.review.presentation.dto;

import com.barofarm.support.review.application.dto.request.ReviewUpdateCommand;
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
    String content
) {
    public ReviewUpdateCommand toCommand(UUID userId, UUID reviewId) {
        return new ReviewUpdateCommand(
            reviewId,
            userId,
            rating,
            reviewVisibility,
            content
        );
    }
}
