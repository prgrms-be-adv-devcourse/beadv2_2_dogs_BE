package com.barofarm.order.order.infrastructure;

import com.barofarm.order.order.domain.Order;
import com.barofarm.order.order.domain.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return orderJpaRepository.findById(id);
    }

    @Override
    public Page<Order> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable) {
        return orderJpaRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
}
