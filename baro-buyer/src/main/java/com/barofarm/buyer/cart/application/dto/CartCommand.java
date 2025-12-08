package com.barofarm.buyer.cart.application.dto;

import java.util.UUID;

/** 장바구니 Command DTO (Service 입력용) */
public class CartCommand {

  /** 상품 추가 Command */
  public record AddItem(UUID productId, Integer quantity, Long unitPrice, String optionInfoJson) {}

  /** 수량 변경 Command */
  public record UpdateQuantity(UUID itemId, Integer quantity) {}

  /** 옵션 변경 Command */
  public record UpdateOption(UUID itemId, String optionInfoJson) {}
}
