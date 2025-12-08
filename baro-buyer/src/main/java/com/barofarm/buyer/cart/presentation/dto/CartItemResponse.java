package com.barofarm.buyer.cart.presentation.dto;

import java.util.UUID;

/** 장바구니 항목 응답 DTO */
public record CartItemResponse(
    UUID itemId,
    UUID productId,
    Integer quantity,
    Long unitPrice,
    String optionInfoJson,
    Long lineTotal) {}
