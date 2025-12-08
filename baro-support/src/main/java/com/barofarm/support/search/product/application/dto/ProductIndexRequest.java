package com.barofarm.support.search.product.application.dto;

import java.util.UUID;

// 상품 색인 요청 DTO (updatedAt은 서버에서 자동 생성)
public record ProductIndexRequest(
    UUID productId, String productName, String productCategory, Long price, String status) {}
