package com.barofarm.buyer.inventory.infrastructure;

import com.barofarm.buyer.inventory.domain.Inventory;
import com.barofarm.buyer.inventory.domain.InventoryRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InventoryRepositoryAdapter implements InventoryRepository {

    private final InventoryJpaRepository inventoryJpaRepository;

    @Override
    public Optional<Inventory> findById(UUID inventoryId) {
        return inventoryJpaRepository.findById(inventoryId);
    }

    @Override
    public List<Inventory> findAllByProductId(UUID productId) {
        return inventoryJpaRepository.findAllByProductId(productId);
    }

    @Override
    public Inventory save(Inventory inventory) {
        return inventoryJpaRepository.save(inventory);
    }

    @Override
    public void delete(Inventory inventory) {
        inventoryJpaRepository.delete(inventory);
    }

    @Override
    public void deleteAllByProductId(UUID productId) {
        inventoryJpaRepository.deleteAllByProductId(productId);
    }
}
