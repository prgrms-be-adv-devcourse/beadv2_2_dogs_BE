package com.barofarm.buyer.product.application.dto;

import com.barofarm.buyer.product.domain.ProductCategory;
import com.barofarm.buyer.product.domain.ProductStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductUpdateCommand(
    UUID memberId,
    String role,
    String productName,
    String description,
    ProductCategory productCategory,
    BigDecimal price,
    Integer stockQuantity,
    ProductStatus productStatus) {}
