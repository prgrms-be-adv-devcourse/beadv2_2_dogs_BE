package com.barofarm.support.search.product.application.dto;

import java.util.UUID;

public record ProductAutoItem(
    UUID productId, // 프론트에서 클릭 시 상품으로 바로 이동
    String productName, // 상품명
    String type // 타입
) {
    public ProductAutoItem(UUID productId, String productName) {
        this(productId, productName, "product");
    }
}
