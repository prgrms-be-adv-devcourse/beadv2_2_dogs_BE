package com.barofarm.buyer.inventory.application.dto.request;

import java.util.List;
import java.util.UUID;

public record InventoryDecreaseCommand(
    List<Item> items
) {
    public record Item(
        UUID productId,
        int quantity
    ) {}
}
