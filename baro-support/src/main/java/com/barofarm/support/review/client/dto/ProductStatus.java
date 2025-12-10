package com.barofarm.support.review.client.dto;

import com.barofarm.support.common.exception.CustomException;
import com.barofarm.support.review.exception.ReviewErrorCode;

public enum ProductStatus {
    ON_SALE(true),
    DISCOUNTED(true),
    SOLD_OUT(true),
    HIDDEN(false);

    private final boolean reviewable;

    ProductStatus(boolean reviewable) {
        this.reviewable = reviewable;
    }

    public boolean isNotReviewable() {
        return !reviewable;
    }

    public static ProductStatus from(String status) {
        try {
            return ProductStatus.valueOf(status);
        } catch (Exception e) {
            throw new CustomException(ReviewErrorCode.INVALID_PRODUCT_STATUS);
        }
    }
}
