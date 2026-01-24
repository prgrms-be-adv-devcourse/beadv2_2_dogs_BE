package com.barofarm.buyer.product.application.dto.internal;

import com.barofarm.buyer.product.domain.Product;
import java.util.UUID;

/**
 * AI 서비스에서 제철 판단에 필요한 최소한의 상품 정보
 */
public record ProductSummary(
    UUID id,
    String productName,
    String categoryCode
) {
    public static ProductSummary from(Product product) {
        return new ProductSummary(
            product.getId(),
            product.getProductName(),
            product.getCategory().getCode()
        );
    }
}
