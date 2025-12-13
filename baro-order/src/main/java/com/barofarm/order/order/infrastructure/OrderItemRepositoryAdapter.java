package com.barofarm.order.order.infrastructure;

import com.barofarm.order.order.domain.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderItemRepositoryAdapter implements OrderItemRepository {

    private final OrderJpaRepository orderJpaRepository;

}
