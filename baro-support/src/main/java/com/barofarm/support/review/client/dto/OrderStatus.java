package com.barofarm.support.review.client.dto;

import com.barofarm.support.common.exception.CustomException;
import com.barofarm.support.review.exception.ReviewErrorCode;

public enum OrderStatus {
    PENDING(false),
    PAID(false),
    PREPARING(false),
    SHIPPED(false),
    COMPLETED(true),
    CANCELED(false);

    private final boolean reviewable;

    OrderStatus(boolean reviewable) {
        this.reviewable = reviewable;
    }

    public boolean isNotReviewable() {
        return !reviewable;
    }

    public static OrderStatus from(String status) {
        try {
            return OrderStatus.valueOf(status);
        } catch (Exception e) {
            throw new CustomException(ReviewErrorCode.INVALID_ORDER_STATUS);
        }
    }
}
