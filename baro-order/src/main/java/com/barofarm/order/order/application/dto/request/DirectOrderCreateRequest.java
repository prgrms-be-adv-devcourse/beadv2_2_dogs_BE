package com.barofarm.order.order.application.dto.request;

import java.util.UUID;

public record DirectOrderCreateRequest(
    UUID productId,
    String address,
    int quantity,
    Long unitPrice
) {
}
