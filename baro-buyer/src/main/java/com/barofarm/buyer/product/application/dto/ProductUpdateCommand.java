package com.barofarm.buyer.product.application.dto;

import com.barofarm.buyer.product.domain.ProductCategory;
import com.barofarm.buyer.product.domain.ProductStatus;
import java.math.BigDecimal;

public record ProductUpdateCommand(
    String productName,
    String description,
    ProductCategory productCategory,
    BigDecimal price,
    Integer stockQuantity,
    ProductStatus productStatus) {}
