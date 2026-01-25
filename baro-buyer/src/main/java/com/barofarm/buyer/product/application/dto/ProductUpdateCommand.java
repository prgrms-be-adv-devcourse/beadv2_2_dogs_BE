package com.barofarm.buyer.product.application.dto;

import com.barofarm.buyer.product.domain.ProductStatus;
import com.barofarm.buyer.product.domain.UserType;
import java.util.List;
import java.util.UUID;

public record ProductUpdateCommand(
    UUID memberId,
    UserType role,
    String productName,
    String description,
    UUID categoryId,
    Long price,
    List<ProductInventoryOptionCommand> inventoryOptions,
    ProductStatus productStatus) {}
