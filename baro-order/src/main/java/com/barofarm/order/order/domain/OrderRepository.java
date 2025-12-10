package com.barofarm.order.order.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

    Order save(Order order);
    Optional<Order> findById(UUID id);
    Page<Order> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
