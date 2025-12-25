package com.barofarm.support.search.product.application.dto;

import java.util.UUID;

// 프론트에 보여줄 상품 List Item
public record ProductSearchItem(
    UUID productId,
    String productName, // 상품명
    String productCategory, // 카테고리
    Long price // 가격
) {
}
