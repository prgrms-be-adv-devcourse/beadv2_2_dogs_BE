package com.barofarm.support.review.domain;

import com.barofarm.support.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
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

    private Review(UUID orderItemId,
                  UUID buyerId,
                  UUID productId,
                  Integer rating,
                  ReviewStatus status,
                  String content) {
        if (orderItemId == null){
            throw new IllegalArgumentException("orderItemId는 필수 값입니다.");
        }

        if (buyerId == null) {
            throw new IllegalArgumentException("buyerId는 필수 값입니다.");
        }
        if (productId == null) {
            throw new IllegalArgumentException("productId는 필수 값입니다.");
        }
        if (rating == null) {
            throw new IllegalArgumentException("rating은 필수 값입니다.");
        }
        if (status == null) {
            throw new IllegalArgumentException("status는 필수 값입니다.");
        }
        if (!isValidRating(rating)) {
            throw new IllegalArgumentException("rating은 1~5 사이여야 합니다.");
        }

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

        return new Review(
            orderItemId,
            buyerId,
            productId,
            rating,
            status,
            content
        );
    }

    public void update(Integer rating, ReviewStatus status, String content) {
        if (rating == null){
            throw new IllegalArgumentException("rating은 필수 값입니다.");
        }
        if (status == null) {
            throw new IllegalArgumentException("status는 필수 값입니다.");
        }
        if (!isValidRating(rating)) {
            throw new IllegalArgumentException("rating은 1~5 사이여야 합니다.");
        }

        this.rating = rating;
        this.status = status;
        this.content = content;
    }

    public boolean isValidRating(int rating) {
        return rating >= 1 && rating <=5;
    }

    public void delete() {
        if (this.status == ReviewStatus.DELETED) {
            throw new IllegalArgumentException("이미 삭제된 리뷰입니다.");
        }
        this.status = ReviewStatus.DELETED;
    }

    public boolean isValidReviewOwner(UUID requesterId) {
        return this.buyerId.equals(requesterId);
    }

    public boolean isValidUserUpdatable() {
        return this.status.isUserEditable();
    }

    public boolean canRead(UUID requesterId) {
        boolean owner = isOwner(requesterId);

        if (owner) {
            return this.status.isVisibleToOwner();
        }

        return this.status.isVisibleToPublic();
    }

    private boolean isOwner(UUID requesterId) {
        return buyerId.equals(requesterId);
    }
}
