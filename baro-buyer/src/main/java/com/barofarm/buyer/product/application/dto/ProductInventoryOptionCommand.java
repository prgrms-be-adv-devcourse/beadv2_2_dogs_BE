package com.barofarm.buyer.product.application.dto;

public record ProductInventoryOptionCommand(
    Long quantity,
    Integer unit
) {
}
