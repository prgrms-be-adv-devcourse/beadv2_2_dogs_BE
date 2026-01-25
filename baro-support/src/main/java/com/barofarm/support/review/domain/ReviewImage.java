package com.barofarm.support.review.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review_image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ReviewImage {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "s3_key", nullable = false, length = 500)
    private String s3Key;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    private ReviewImage(Review review, String imageUrl, String s3Key, Integer sortOrder) {
        this.id = UUID.randomUUID();
        this.review = review;
        this.imageUrl = imageUrl;
        this.s3Key = s3Key;
        this.sortOrder = sortOrder;
    }

    public static ReviewImage create(Review review, String imageUrl, String s3Key, Integer sortOrder) {
        return new ReviewImage(review, imageUrl, s3Key, sortOrder);
    }
}
