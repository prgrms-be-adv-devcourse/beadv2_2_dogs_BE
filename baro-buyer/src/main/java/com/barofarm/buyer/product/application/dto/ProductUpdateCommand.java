package com.barofarm.buyer.product.application.dto;

import com.barofarm.buyer.product.domain.ProductCategory;
import com.barofarm.buyer.product.domain.ProductStatus;
import java.util.UUID;

public record ProductUpdateCommand(
    UUID memberId,
    String role,
    String productName,
    String description,
    ProductCategory productCategory,
    Integer price,
    Integer stockQuantity,
    ProductStatus productStatus) {}
