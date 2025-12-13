package com.barofarm.order.order.presentation.dto;

import java.util.List;
import java.util.UUID;

public class InventoryIncreaseRequest {

    private List<Item> items;

    public InventoryIncreaseRequest(List<Item> items) {
        this.items = items;
    }

    public List<Item> getItems() {
        return items;
    }

    public static class Item {
        private UUID productId;
        private int quantity;

        public Item(UUID productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public UUID getProductId() {
            return productId;
        }

        public int getQuantity() {
            return quantity;
        }
    }
}
