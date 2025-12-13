package com.barofarm.buyer.cart.application.dto;

import com.barofarm.buyer.cart.domain.CartItem;
import java.util.UUID;

// 장바구니의 개별 상품 조회용 DTO
public record CartItemInfo(
    UUID itemId,
    UUID productId,
    Integer quantity,
    Long unitPrice,
    Long lineTotalPrice,
    String optionInfoJson
) {
    public static CartItemInfo from(CartItem item) {
        return new CartItemInfo(
            item.getId(),
            item.getProductId(),
            item.getQuantity(),
            item.getUnitPrice(),
            item.calculatePrice(),      // CartItem 도메인 메서드 사용
            item.getOptionInfoJson()
        );
    }
}
