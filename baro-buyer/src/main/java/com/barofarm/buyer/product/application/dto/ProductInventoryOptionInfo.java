package com.barofarm.buyer.product.application.dto;

import java.util.UUID;

public record ProductInventoryOptionInfo(
    UUID inventoryId,
    Long quantity,
    Integer unit
) {
}
