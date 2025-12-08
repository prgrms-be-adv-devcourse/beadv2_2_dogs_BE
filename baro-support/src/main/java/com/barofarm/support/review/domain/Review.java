package com.barofarm.support.review.domain;

import com.barofarm.support.common.entity.BaseEntity;
import com.barofarm.support.common.exception.CustomException;
import com.barofarm.support.review.exception.ReviewErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Review extends BaseEntity {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "order_item_id", nullable = false)
    private UUID orderItemId;

    @Column(name = "buyer_id", nullable = false)
    private UUID buyerId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private Integer rating;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReviewStatus status;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder
    private Review(UUID orderItemId,
                  UUID buyerId,
                  UUID productId,
                  Integer rating,
                  ReviewStatus status,
                  String content) {
        if (orderItemId == null) {
            throw new CustomException(ReviewErrorCode.ORDER_ITEM_ID_NULL);
        }
        if (buyerId == null) {
            throw new CustomException(ReviewErrorCode.BUYER_ID_NULL);
        }
        if (productId == null) {
            throw new CustomException(ReviewErrorCode.PRODUCT_ID_NULL);
        }
        if (rating == null) {
            throw new CustomException(ReviewErrorCode.RATING_NULL);
        }
        if (status == null) {
            throw new CustomException(ReviewErrorCode.STATUS_NULL);
        }
        validateRating(rating);

        this.id = UUID.randomUUID();
        this.orderItemId = orderItemId;
        this.buyerId = buyerId;
        this.productId = productId;
        this.rating = rating;
        this.status = status;
        this.content = content;
    }

    public static Review create(UUID orderItemId,
                                UUID buyerId,
                                UUID productId,
                                Integer rating,
                                ReviewStatus status,
                                String content) {
        return Review.builder()
            .orderItemId(orderItemId)
            .buyerId(buyerId)
            .productId(productId)
            .rating(rating)
            .status(status != null ? status : ReviewStatus.defaultStatus())
            .content(content)
            .build();
    }

    public void update(Integer rating,
                       ReviewStatus status,
                       String content) {
        if (rating == null) {
            throw new CustomException(ReviewErrorCode.RATING_NULL);
        }
        if (status == null) {
            throw new CustomException(ReviewErrorCode.STATUS_NULL);
        }

        validateRating(rating);

        this.rating = rating;
        this.status = status;
        this.content = content;
    }

    private void validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new CustomException(ReviewErrorCode.INVALID_RATING_VALUE);
        }
    }
}
