package com.barofarm.support.delivery.infrastructure;

import com.barofarm.support.delivery.domain.Delivery;
import com.barofarm.support.delivery.domain.DeliveryRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DeliveryRepositoryAdapter implements DeliveryRepository {

    private final DeliveryJpaRepository deliveryJpaRepository;

    @Override
    public Optional<Delivery> findById(UUID id) {
        return deliveryJpaRepository.findById(id);
    }

    @Override
    public Optional<Delivery> findByOrderId(UUID orderId) {
        return deliveryJpaRepository.findByOrderId(orderId);
    }

    @Override
    public boolean existsByOrderId(UUID orderId) {
        return deliveryJpaRepository.existsByOrderId(orderId);
    }

    @Override
    public Delivery save(Delivery delivery) {
        return deliveryJpaRepository.save(delivery);
    }
}
