package com.barofarm.buyer.inventory.domain;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface InventoryRepository {

    List<Inventory> findByProductIdInForUpdate(Set<UUID> productIds);
}
