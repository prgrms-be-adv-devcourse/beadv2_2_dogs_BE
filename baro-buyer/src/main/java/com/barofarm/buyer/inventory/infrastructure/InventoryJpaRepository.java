package com.barofarm.buyer.inventory.infrastructure;

import com.barofarm.buyer.inventory.domain.Inventory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryJpaRepository extends JpaRepository<Inventory, UUID> {
    List<Inventory> findAllByProductId(UUID productId);

    void deleteAllByProductId(UUID productId);
}
