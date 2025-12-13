package com.barofarm.buyer.cart.application.dto;

import com.barofarm.buyer.cart.domain.Cart;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// 장바구니 조회 Info DTO (Service 출력용)
public record CartInfo(
    UUID cartId,
    UUID buyerId,
    List<CartItemInfo> items,
    Long totalPrice,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static CartInfo empty() {
        return new CartInfo(
            null,
            null,
            List.of(),
            0L,
            null,
            null
        );
    }

    public static CartInfo from(Cart cart) {
        return new CartInfo(
            cart.getId(),
            cart.getBuyerId(),
            cart.getItems().stream()
                .map(CartItemInfo::from)
                .toList(),
            cart.calculateTotalPrice(),
            cart.getCreatedAt(),
            cart.getUpdatedAt()
        );
    }
}
