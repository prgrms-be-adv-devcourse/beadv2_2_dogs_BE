package com.barofarm.buyer.inventory.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository {

    Optional<Inventory> findById(UUID inventoryId);

    List<Inventory> findAllByProductId(UUID productId);

    Inventory save(Inventory inventory);

    void delete(Inventory inventory);

    void deleteAllByProductId(UUID productId);
}
