package com.barofarm.buyer.cart.application.dto;

import com.barofarm.buyer.cart.domain.Cart;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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

    // 실시간 상품명, 카테고리 코드/명, 재고 단위로 CartInfo 생성
    public static CartInfo from(Cart cart,
                               Map<UUID, String> productNameMap,
                               Map<UUID, String> productCategoryCodeMap,
                               Map<UUID, String> productCategoryNameMap,
                               Map<UUID, Integer> inventoryUnitMap) {
        return new CartInfo(
            cart.getId(),
            cart.getBuyerId(),
            cart.getItems().stream()
                .map(item -> {
                    UUID productId = item.getProductId();
                    String realTimeName = productNameMap.get(productId);
                    String categoryCode = productCategoryCodeMap.get(productId);
                    String categoryName = productCategoryNameMap.get(productId);
                    Integer unit = inventoryUnitMap.get(item.getInventoryId());
                    return CartItemInfo.from(item, realTimeName, categoryName, categoryCode, unit);
                })
                .toList(),
            cart.calculateTotalPrice(),
            cart.getCreatedAt(),
            cart.getUpdatedAt()
        );
    }
}
