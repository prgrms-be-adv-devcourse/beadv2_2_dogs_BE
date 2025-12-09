package com.barofarm.buyer.product.application.dto;

import com.barofarm.buyer.product.domain.Product;
import com.barofarm.buyer.product.domain.ProductCategory;
import com.barofarm.buyer.product.domain.ProductStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductDetailInfo(
    UUID id,
    UUID sellerId,
    String productName,
    String description,
    ProductCategory productCategory,
    Long price,
    Integer stockQuantity,
    ProductStatus productStatus,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  public static ProductDetailInfo from(Product product) {
    return new ProductDetailInfo(
        product.getId(),
        product.getSellerId(),
        product.getProductName(),
        product.getDescription(),
        product.getProductCategory(),
        product.getPrice(),
        product.getStockQuantity(),
        product.getProductStatus(),
        product.getCreatedAt(),
        product.getUpdatedAt());
  }
}
