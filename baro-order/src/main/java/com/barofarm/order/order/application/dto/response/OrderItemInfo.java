package com.barofarm.order.order.application.dto.response;

import com.barofarm.order.order.domain.OrderItem;
import java.util.UUID;

public record OrderItemInfo(
    UUID id,
    UUID productId,
    String productName,
    String categoryCode,
    UUID sellerId,
    Long quantity,
    Long unitPrice,
    Long totalPrice,
    UUID inventoryId
) {
    public static OrderItemInfo from(OrderItem item) {
        return new OrderItemInfo(
            item.getId(),
            item.getProductId(),
            item.getProductName(),
            item.getCategoryCode(),
            item.getSellerId(),
            item.getQuantity(),
            item.getUnitPrice(),
            item.getTotalPrice(),
            item.getInventoryId()
        );
    }
}
