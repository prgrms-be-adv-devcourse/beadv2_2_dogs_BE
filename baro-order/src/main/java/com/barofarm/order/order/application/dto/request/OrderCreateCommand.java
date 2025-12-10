package com.barofarm.order.order.application.dto.request;

import java.util.List;
import java.util.UUID;

public record OrderCreateCommand(
    String address,
    List<OrderCreateCommand.OrderItemCreateCommand> items
) {
    public record OrderItemCreateCommand(
        UUID productId,
        int quantity,
        long unitPrice
    ) {
    }
}
