package com.barofarm.buyer.cart.application.dto;

import java.util.List;
import java.util.UUID;

/** 장바구니 조회 Info DTO (Service 출력용) */
public record CartInfo(UUID cartId, UUID buyerId, List<CartItemInfo> items, Long totalPrice) {

  /** 장바구니 항목 Info */
  public record CartItemInfo(
      UUID itemId,
      UUID productId,
      Integer quantity,
      Long unitPrice,
      String optionInfoJson,
      Long lineTotal) {}
}
