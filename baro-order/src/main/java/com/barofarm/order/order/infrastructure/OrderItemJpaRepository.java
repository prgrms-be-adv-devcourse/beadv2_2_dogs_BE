package com.barofarm.order.order.infrastructure;

import com.barofarm.order.order.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface OrderItemJpaRepository extends JpaRepository<OrderItem, UUID> {
}
