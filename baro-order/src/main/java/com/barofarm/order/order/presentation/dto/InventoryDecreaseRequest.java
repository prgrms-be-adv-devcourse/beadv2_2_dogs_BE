package com.barofarm.order.order.presentation.dto;

import java.util.List;
import java.util.UUID;

public record InventoryDecreaseRequest(
    List<Item> items
) {
    public record Item(
        UUID productId,
        int quantity
    ) {}
}
