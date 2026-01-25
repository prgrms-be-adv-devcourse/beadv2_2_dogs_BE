package com.barofarm.ai.recommend.infrastructure.client.dto;

import java.util.UUID;

// 장바구니의 개별 상품 정보 DTO (Buyer Service의 CartItemInfo와 동일한 구조)
public record CartItemInfo(
    UUID itemId,
    UUID productId,
    String productName,
    String productCategoryName,
    String productCategoryCode,
    Integer quantity,
    Long unitPrice,
    Long lineTotalPrice,
    UUID inventoryId,
    Integer unit
) {
}
