package com.barofarm.support.delivery.application.dto;

import com.barofarm.support.delivery.domain.Address;
import java.util.UUID;

public record DeliveryCreateCommand(
    UUID orderId,
    Address address
) { }
