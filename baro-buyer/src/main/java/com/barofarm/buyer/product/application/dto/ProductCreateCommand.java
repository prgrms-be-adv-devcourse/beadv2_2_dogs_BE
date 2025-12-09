package com.barofarm.buyer.product.application.dto;

import com.barofarm.buyer.product.domain.ProductCategory;
import java.util.UUID;

public record ProductCreateCommand(
    UUID sellerId,
    String role,
    String productName,
    String description,
    ProductCategory productCategory,
    Long price,
    Integer stockQuantity) {}
