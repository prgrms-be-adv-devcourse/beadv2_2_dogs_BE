package com.barofarm.support.review.client.order.dto;

import java.util.Optional;

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

    public static Optional<OrderStatus> from(String status) {
        try {
            return Optional.of(OrderStatus.valueOf(status));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
