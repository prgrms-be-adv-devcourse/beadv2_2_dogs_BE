package com.barofarm.order.order.application.dto.request;

import java.util.UUID;

public record DeliveryInternalCreateRequest(
    UUID orderId,
    Address address
) {
    public record Address(
        String receiverName,
        String zipCode,
        String address,
        String addressDetail,
        String deliveryMemo
    ) {}
}
