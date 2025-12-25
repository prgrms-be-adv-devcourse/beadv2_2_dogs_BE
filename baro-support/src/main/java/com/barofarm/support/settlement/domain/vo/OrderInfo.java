package com.barofarm.support.settlement.domain.vo;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderInfo(
    UUID orderId,
    UUID orderItemId,
    UUID sellerId,
    UUID productId,
    Long amount,
    Integer quantity,
    Long itemPrice,
    LocalDateTime orderDate,
    LocalDateTime canceledAt
) {}
