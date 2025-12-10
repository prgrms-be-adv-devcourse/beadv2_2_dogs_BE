package com.barofarm.order.order.application.dto.response;

import com.barofarm.order.order.domain.Order;
import com.barofarm.order.order.domain.OrderStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderDetailInfo(
    UUID orderId,
    long totalAmount,
    String address,
    OrderStatus status,
    LocalDateTime createdAt
) {
    public static OrderDetailInfo from(Order order) {
        return new OrderDetailInfo(
            order.getId(),
            order.getTotalAmount(), // 엔티티에 맞게 필드 이름 조정
            order.getAddress(),
            order.getStatus(),
            order.getCreatedAt()
        );
    }
}
