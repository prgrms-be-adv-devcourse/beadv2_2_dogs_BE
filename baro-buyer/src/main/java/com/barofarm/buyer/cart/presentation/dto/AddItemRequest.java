package com.barofarm.buyer.cart.presentation.dto;

import com.barofarm.buyer.cart.application.dto.AddItemCommand;
import java.util.UUID;

// 장바구니에 상품 추가 요청 DTO
public record AddItemRequest(
    UUID productId, Integer quantity, Long unitPrice, String optionInfoJson) {

  public AddItemCommand toCommand() {
    return new AddItemCommand(productId, quantity, unitPrice, optionInfoJson);
  }
}
