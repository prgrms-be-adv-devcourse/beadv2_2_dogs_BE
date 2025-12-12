package com.barofarm.order.order.application.dto.response;

import com.barofarm.order.order.domain.Order;
import com.barofarm.order.order.domain.OrderStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderDetailInfo(
    UUID orderId,
    long totalAmount,
    String receiverName,
    String phone,
    String email,
    String zipCode,
    String address,
    String addressDetail,
    String deliveryMemo,
    OrderStatus status,
    LocalDateTime createdAt
) {
    public static OrderDetailInfo from(Order order) {
        return new OrderDetailInfo(
            order.getId(),
            order.getTotalAmount(),
            order.getReceiverName(),
            order.getPhone(),
            order.getEmail(),
            order.getZipCode(),
            order.getAddress(),
            order.getAddressDetail(),
            order.getDeliveryMemo(),
            order.getStatus(),
            order.getCreatedAt()
        );
    }
}
