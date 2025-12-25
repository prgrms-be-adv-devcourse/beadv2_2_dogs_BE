package com.barofarm.order.order.domain;

import com.barofarm.order.common.response.CustomPage;
import com.barofarm.order.order.application.dto.response.OrderItemSettlementResponse;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface OrderItemRepository {
    CustomPage<OrderItemSettlementResponse> findOrderItemsForSettlement(
        LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable);
    Optional<OrderItem> findById(UUID id);
}
