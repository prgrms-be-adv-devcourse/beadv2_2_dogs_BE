package com.barofarm.order.order.application.dto.request;

import java.util.List;
import java.util.UUID;

public record OrderCreateCommand(
    String receiverName,
    String phone,
    String email,
    String zipCode,
    String address,
    String addressDetail,
    String deliveryMemo,   // 선택값
    List<OrderItemCreateCommand> items
) {
    public record OrderItemCreateCommand(
        UUID productId,
        int quantity,
        long unitPrice
    )
    {}
}
