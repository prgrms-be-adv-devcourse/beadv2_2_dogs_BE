package com.barofarm.buyer.inventory.presentation.dto;

import com.barofarm.buyer.inventory.domain.Inventory;
import java.util.UUID;

public record InventoryInfo(
    UUID inventoryId,
    Long quantity,
    Long reservedQuantity,
    Integer unit
) {

    public static InventoryInfo from(Inventory inventory) {
        return new InventoryInfo(
            inventory.getId(),
            inventory.getQuantity(),
            inventory.getReservedQuantity(),
            inventory.getUnit()
        );
    }
}
