package com.barofarm.order.order.infrastructure;

import com.barofarm.order.order.application.dto.response.OrderItemSettlementResponse;
import com.barofarm.order.order.domain.OrderItem;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderItemJpaRepository extends JpaRepository<OrderItem, UUID> {

    @Query("""
        select new com.barofarm.order.order.application.dto.response.OrderItemSettlementResponse(
            o.id,
            oi.id,
            oi.sellerId,
            oi.productId,
            oi.totalPrice,
            oi.quantity,
            o.createdAt,
            o.canceledAt
        )
        from OrderItem oi
        join oi.order o
        where o.createdAt between :startDateTime and :endDateTime
    """)
    Page<OrderItemSettlementResponse> findOrderItemsForSettlement(
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        Pageable pageable
    );
}
