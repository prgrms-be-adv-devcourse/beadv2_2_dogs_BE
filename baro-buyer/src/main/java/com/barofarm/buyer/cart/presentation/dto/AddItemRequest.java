package com.barofarm.buyer.cart.presentation.dto;

import java.util.UUID;

/** 장바구니에 상품 추가 요청 DTO */
public record AddItemRequest(
    UUID productId, Integer quantity, Long unitPrice, String optionInfoJson) {}
