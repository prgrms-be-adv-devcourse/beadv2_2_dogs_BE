package com.barofarm.support.delivery.presentation.dto.internal;

import com.barofarm.support.delivery.domain.Address;
import java.util.UUID;

public record DeliveryInternalCreateRequest(
    UUID orderId,
    Address address
) { }
