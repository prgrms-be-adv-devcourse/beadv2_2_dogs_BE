package com.barofarm.order.order.domain;

import com.barofarm.order.common.response.CustomPage;
import com.barofarm.order.order.application.dto.response.OrderItemSettlementResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface OrderItemRepository {
    CustomPage<OrderItemSettlementResponse> findOrderItemsForSettlement(LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable);
}
