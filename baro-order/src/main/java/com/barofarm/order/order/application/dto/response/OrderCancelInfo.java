package com.barofarm.order.order.application.dto.response;

import com.barofarm.order.order.domain.Order;
import com.barofarm.order.order.domain.OrderStatus;
import java.util.UUID;

public record OrderCancelInfo(
    UUID orderId,
    OrderStatus status
) {
    public static OrderCancelInfo from(Order order) {
        return new OrderCancelInfo(order.getId(), order.getStatus());
    }
}
