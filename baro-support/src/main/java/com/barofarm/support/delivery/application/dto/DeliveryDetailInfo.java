package com.barofarm.support.delivery.application.dto;

import com.barofarm.support.delivery.domain.Address;
import com.barofarm.support.delivery.domain.Delivery;
import com.barofarm.support.delivery.domain.DeliveryStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record DeliveryDetailInfo(
    UUID deliveryId,
    UUID orderId,
    DeliveryStatus deliveryStatus,
    String courier,
    String trackingNumber,
    Address address,
    LocalDateTime shippedAt,
    LocalDateTime deliveredAt
) {
    public static DeliveryDetailInfo from(Delivery delivery) {
        return new DeliveryDetailInfo(
            delivery.getId(),
            delivery.getOrderId(),
            delivery.getDeliveryStatus(),
            delivery.getCourier(),
            delivery.getTrackingNumber(),
            delivery.getAddress(),
            delivery.getShippedAt(),
            delivery.getDeliveredAt()
        );
    }
}
