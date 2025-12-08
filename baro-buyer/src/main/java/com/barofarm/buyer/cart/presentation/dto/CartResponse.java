package com.barofarm.buyer.cart.presentation.dto;

import java.util.List;
import java.util.UUID;

/** 장바구니 조회 응답 DTO */
public record CartResponse(
    UUID cartId, UUID buyerId, List<CartItemResponse> items, Long totalPrice) {}
