package com.barofarm.support.review.application.dto.request;

import com.barofarm.support.review.domain.ReviewStatus;
import com.barofarm.support.review.presentation.dto.ReviewVisibility;
import java.util.UUID;

public record ReviewCreateCommand(
    UUID orderItemId,
    UUID userId,
    UUID productId,
    Integer rating,
    ReviewVisibility visibility,
    String content) {

    public ReviewStatus toReviewStatus() {
        return ReviewStatus.fromVisibility(visibility);
    }
}
