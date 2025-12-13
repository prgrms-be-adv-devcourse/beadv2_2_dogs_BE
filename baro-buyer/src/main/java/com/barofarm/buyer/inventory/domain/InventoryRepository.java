package com.barofarm.buyer.inventory.domain;

import java.util.*;
import java.util.UUID;

public interface InventoryRepository {

    List<Inventory> findByProductIdInForUpdate(Set<UUID> productIds);
}
