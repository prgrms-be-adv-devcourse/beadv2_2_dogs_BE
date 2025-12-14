package com.barofarm.support.delivery.application;

import com.barofarm.support.common.exception.CustomException;
import com.barofarm.support.delivery.application.dto.DeliveryCreateCommand;
import com.barofarm.support.delivery.domain.Delivery;
import com.barofarm.support.delivery.domain.DeliveryRepository;
import com.barofarm.support.delivery.exception.DeliveryErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;

    @Transactional
    public Delivery createDelivery(DeliveryCreateCommand command) {
        if (deliveryRepository.existsByOrderId(command.orderId())) {
            throw new CustomException(DeliveryErrorCode.DELIVERY_ALREADY_EXISTS);
        }

        Delivery delivery = Delivery.create(
            command.orderId(),
            command.address()
        );

        return deliveryRepository.save(delivery);
    }
}
