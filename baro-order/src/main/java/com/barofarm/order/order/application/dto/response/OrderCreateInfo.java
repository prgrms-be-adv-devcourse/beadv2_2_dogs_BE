package com.barofarm.order.order.application.dto.response;

import com.barofarm.order.order.domain.Order;
import java.util.UUID;

public record OrderCreateInfo(
    UUID orderId,
    Long totalAmount
) {

    public static OrderCreateInfo from(Order order) {
        return new OrderCreateInfo(
            order.getId(),
            order.getTotalAmount()
        );
    }
}
