package com.barofarm.support.delivery.presentation.dto;

import com.barofarm.support.delivery.application.dto.ShipDeliveryCommand;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record ShipDeliveryRequest(
    @NotBlank(message = "택배사는 필수입니다.")
    String courier,

    @NotBlank(message = "운송장 번호는 필수입니다.")
    String trackingNumber
) {
    public ShipDeliveryCommand toCommand(UUID id, UUID userId, String role) {
        return new ShipDeliveryCommand(id, userId, role, courier, trackingNumber);
    }
}
