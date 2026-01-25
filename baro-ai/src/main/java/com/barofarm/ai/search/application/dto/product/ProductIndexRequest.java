package com.barofarm.ai.search.application.dto.product;

import java.util.UUID;

// 상품 색인 요청 DTO (updatedAt은 서버에서 자동 생성)
public record ProductIndexRequest(
    UUID productId,
    String productName,
    UUID productCategoryId,
    String productCategoryCode,
    String productCategoryName,
    Long price,
    String status) {}
