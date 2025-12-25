package com.barofarm.support.review.client.product.dto;

import java.util.Optional;

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

    public static Optional<ProductStatus> from(String status) {
        try {
            return Optional.of(ProductStatus.valueOf(status));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
