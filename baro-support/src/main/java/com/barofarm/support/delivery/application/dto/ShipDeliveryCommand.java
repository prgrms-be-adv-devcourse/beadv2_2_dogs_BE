package com.barofarm.support.delivery.application.dto;

import java.util.UUID;

public record ShipDeliveryCommand(
    UUID id,
    UUID userId,
    String role,
    String courier,
    String trackingNumber
) {
}
