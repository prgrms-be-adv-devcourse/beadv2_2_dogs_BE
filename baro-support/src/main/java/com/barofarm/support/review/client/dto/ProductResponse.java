package com.barofarm.support.review.client.dto;

import java.util.UUID;

public record ProductResponse(
    UUID productId,
    String status
) {
    public boolean isAvailableForReview() {
        return "ON_SALE".equals(status) || "DISCOUNTED".equals(status);
    }
}
