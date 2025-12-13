package com.barofarm.buyer.cart.application.dto;

import java.util.UUID;

// 장바구니에 상품 추가 Command DTO
public record AddItemCommand(
    UUID productId,
    Integer quantity,
    Long unitPrice,
    String optionInfoJson
) {}
