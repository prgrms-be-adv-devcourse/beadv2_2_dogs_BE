package com.barofarm.order.order.infrastructure;

import com.barofarm.order.order.domain.OrderItem;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemJpaRepository extends JpaRepository<OrderItem, UUID> {
}
