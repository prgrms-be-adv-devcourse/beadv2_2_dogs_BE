package com.barofarm.support.review.application.dto.response;

import com.barofarm.support.review.domain.Review;
import com.barofarm.support.review.domain.ReviewStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewDetailInfo(
    UUID id,
    UUID orderItemId,
    UUID buyerId,
    UUID productId,
    Integer rating,
    ReviewStatus status,
    String content,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ReviewDetailInfo from(Review review) {
        return new ReviewDetailInfo(
            review.getId(),
            review.getOrderItemId(),
            review.getBuyerId(),
            review.getProductId(),
            review.getRating(),
            review.getStatus(),
            review.getContent(),
            review.getCreatedAt(),
            review.getUpdatedAt()
        );
    }
}
