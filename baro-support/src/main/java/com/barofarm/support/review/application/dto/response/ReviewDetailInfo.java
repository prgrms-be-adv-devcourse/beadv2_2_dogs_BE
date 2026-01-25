package com.barofarm.support.review.application.dto.response;

import com.barofarm.support.review.domain.Review;
import com.barofarm.support.review.domain.ReviewImage;
import com.barofarm.support.review.domain.ReviewStatus;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record ReviewDetailInfo(
    UUID id,
    UUID orderItemId,
    UUID buyerId,
    UUID productId,
    Integer rating,
    ReviewStatus status,
    String content,
    List<String> imageUrls,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ReviewDetailInfo from(Review review) {
        List<String> imageUrls = review.getImages()
            .stream()
            .sorted(Comparator.comparingInt(ReviewImage::getSortOrder))
            .map(ReviewImage::getImageUrl)
            .toList();

        return new ReviewDetailInfo(
            review.getId(),
            review.getOrderItemId(),
            review.getBuyerId(),
            review.getProductId(),
            review.getRating(),
            review.getStatus(),
            review.getContent(),
            imageUrls,
            review.getCreatedAt(),
            review.getUpdatedAt()
        );
    }
}
