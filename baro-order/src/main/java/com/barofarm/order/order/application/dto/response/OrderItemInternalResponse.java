package com.barofarm.order.order.application.dto.response;

import com.barofarm.order.order.domain.OrderItem;
import java.util.UUID;

public record OrderItemInternalResponse(
    UUID orderItemId,
    UUID productId,
    UUID sellerId,
    UUID buyerId,
    String status
) {
    public static OrderItemInternalResponse from(OrderItem orderItem) {
        return new OrderItemInternalResponse(
            orderItem.getId(),
            orderItem.getProductId(),
            orderItem.getSellerId(),
            orderItem.getOrder().getUserId(),
            orderItem.getOrder().getStatus().name()
        );
    }
}
