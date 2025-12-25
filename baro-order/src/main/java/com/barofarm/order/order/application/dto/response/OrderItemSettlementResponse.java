package com.barofarm.order.order.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderItemSettlementResponse(
    UUID orderId,
    UUID orderItemId,
    UUID sellerId,
    UUID productId,
    Long amount,
    Integer quantity,
    LocalDateTime orderDate,
    LocalDateTime canceledAt
) {}
