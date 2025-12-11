package com.barofarm.buyer.inventory.infrastructure;

import com.barofarm.buyer.inventory.domain.Inventory;
import com.barofarm.buyer.inventory.domain.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class InventoryRepositoryAdapter implements InventoryRepository {

    private final InventoryJpaRepository inventoryJpaRepository;

    @Override
    public List<Inventory> findByProductIdInForUpdate(Set<UUID> productIds) {
        return inventoryJpaRepository.findByProductIdIn(productIds);
    }
}
