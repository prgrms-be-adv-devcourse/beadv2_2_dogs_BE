package com.barofarm.support.review.application.dto.request;

import com.barofarm.support.review.domain.ReviewStatus;
import java.util.UUID;

public record ReviewCreateCommand(
    UUID orderItemId,
    UUID buyerId,
    UUID productId,
    Integer rating,
    ReviewStatus reviewStatus,
    String content) {
}
