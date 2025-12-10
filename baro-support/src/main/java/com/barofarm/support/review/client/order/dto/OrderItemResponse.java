package com.barofarm.support.review.client.order.dto;

import java.util.UUID;

public record OrderItemResponse(
    UUID orderItemId,
    UUID productId,
    UUID sellerId,
    UUID buyerId,
    String status
) {}
