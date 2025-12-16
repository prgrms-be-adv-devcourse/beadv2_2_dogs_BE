package com.barofarm.order.order.application.dto.response;

import com.barofarm.order.order.domain.Order;
import com.barofarm.order.order.domain.OrderStatus;
import java.util.List;
import java.util.UUID;

public record OrderCreateInfo(
    UUID orderId,
    Long totalAmount,
    OrderStatus status,
    String receiverName,
    String phone,
    String email,
    String zipCode,
    String address,
    String addressDetail,
    String deliveryMemo,
    int itemCount,
    List<OrderItemInfo> items
) {
    public static OrderCreateInfo from(Order order) {
        return new OrderCreateInfo(
            order.getId(),
            order.getTotalAmount(),
            order.getStatus(),
            order.getReceiverName(),
            order.getPhone(),
            order.getEmail(),
            order.getZipCode(),
            order.getAddress(),
            order.getAddressDetail(),
            order.getDeliveryMemo(),
            order.getOrderItems().size(),
            order.getOrderItems().stream().map(OrderItemInfo::from).toList()
        );
    }
}
