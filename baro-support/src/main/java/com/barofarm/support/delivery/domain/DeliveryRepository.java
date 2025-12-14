package com.barofarm.support.delivery.domain;

import java.util.Optional;
import java.util.UUID;

public interface DeliveryRepository {
    Optional<Delivery> findByOrderId(UUID orderId);
}
