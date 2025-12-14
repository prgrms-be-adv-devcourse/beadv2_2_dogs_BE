package com.barofarm.buyer.product.application.dto.internal;

import com.barofarm.buyer.product.domain.Product;
import com.barofarm.buyer.product.domain.ProductStatus;
import java.util.UUID;

public record ReviewProductInfo(
    UUID productId,
    ProductStatus status
) {
    public static ReviewProductInfo from(Product product) {
        return new ReviewProductInfo(
            product.getId(),
            product.getProductStatus()
        );
    }
}
