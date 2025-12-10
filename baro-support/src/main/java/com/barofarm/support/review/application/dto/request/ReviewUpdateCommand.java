package com.barofarm.support.review.application.dto.request;

import com.barofarm.support.review.domain.ReviewStatus;
import java.util.UUID;

public record ReviewUpdateCommand(
    UUID reviewId,
    UUID buyerId,
    Integer rating,
    ReviewStatus reviewStatus,
    String content) {
}
