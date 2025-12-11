package com.barofarm.buyer.inventory.infrastructure;

import com.barofarm.buyer.inventory.domain.Inventory;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface InventoryJpaRepository extends JpaRepository<Inventory, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> findByProductIdIn(Set<UUID> productIds);
}
