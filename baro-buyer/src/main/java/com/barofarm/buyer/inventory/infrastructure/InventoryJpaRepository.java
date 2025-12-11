package com.barofarm.buyer.inventory.infrastructure;

import com.barofarm.buyer.inventory.domain.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface InventoryJpaRepository extends JpaRepository<Inventory, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> findByProductIdIn(Set<UUID> productIds);
}
