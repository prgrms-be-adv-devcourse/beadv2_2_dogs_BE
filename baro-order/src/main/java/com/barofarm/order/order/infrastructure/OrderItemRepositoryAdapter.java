package com.barofarm.order.order.infrastructure;

import com.barofarm.order.common.response.CustomPage;
import com.barofarm.order.order.application.dto.response.OrderItemSettlementResponse;
import com.barofarm.order.order.domain.OrderItem;
import com.barofarm.order.order.domain.OrderItemRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderItemRepositoryAdapter implements OrderItemRepository {

    private final OrderItemJpaRepository orderItemJpaRepository;


    @Override
    public CustomPage<OrderItemSettlementResponse> findOrderItemsForSettlement(
        LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable) {
        Page<OrderItemSettlementResponse> page = orderItemJpaRepository
            .findOrderItemsForSettlement(startDateTime, endDateTime, pageable);
        return CustomPage.from(page);
    }

    @Override
    public Optional<OrderItem> findById(UUID id) {
        return orderItemJpaRepository.findById(id);
    }
}
