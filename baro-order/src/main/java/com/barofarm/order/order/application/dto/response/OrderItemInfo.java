package com.barofarm.order.order.application.dto.response;

import com.barofarm.order.order.domain.OrderItem;
import java.util.UUID;

public record OrderItemInfo(
    UUID id,
    UUID productId,
    UUID sellerId,
    int quantity,
    Long unitPrice,
    Long totalPrice
) {
    public static OrderItemInfo from(OrderItem item) {
        return new OrderItemInfo(
            item.getId(),
            item.getProductId(),
            item.getSellerId(),
            item.getQuantity(),
            item.getUnitPrice(),
            item.getTotalPrice()
        );
    }
}
