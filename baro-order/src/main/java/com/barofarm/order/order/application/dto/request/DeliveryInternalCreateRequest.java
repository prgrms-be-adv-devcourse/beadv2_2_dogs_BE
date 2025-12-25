package com.barofarm.order.order.application.dto.request;

import com.barofarm.order.order.application.dto.response.OrderDeliveryInfo;
import java.util.UUID;

public record DeliveryInternalCreateRequest(
        UUID orderId,
        Address address
) {

    public static DeliveryInternalCreateRequest from(OrderDeliveryInfo info) {
        return new DeliveryInternalCreateRequest(
                info.orderId(),
                new Address(
                        info.receiverName(),
                        info.phone(),
                        info.zipCode(),
                        info.address(),
                        info.addressDetail()
                )
        );
    }

    public record Address(
            String receiverName,
            String receiverPhone,
            String postalCode,
            String addressLine1,
            String addressLine2
    ) {}
}
