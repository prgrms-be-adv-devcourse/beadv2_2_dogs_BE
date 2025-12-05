package com.barofarm.support.search.product.application.dto;

// 상품 색인 요청 DTO (updatedAt은 서버에서 자동 생성)
public record ProductIndexRequest(
    String productId, String productName, String category, Integer price, String status) {}
