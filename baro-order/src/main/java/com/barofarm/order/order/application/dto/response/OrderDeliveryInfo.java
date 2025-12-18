package com.barofarm.order.order.application.dto.response;

import com.barofarm.order.order.domain.Order;
import java.util.UUID;

public record OrderDeliveryInfo(
    UUID orderId,
    String receiverName,
    String phone,
    String zipCode,
    String address,
    String addressDetail,
    String deliveryMemo
) {
    public static OrderDeliveryInfo from(Order order) {
        return new OrderDeliveryInfo(
            order.getId(),
            order.getReceiverName(),
            order.getPhone(),
            order.getZipCode(),
            order.getAddress(),
            order.getAddressDetail(),
            order.getDeliveryMemo()
        );
    }
}
