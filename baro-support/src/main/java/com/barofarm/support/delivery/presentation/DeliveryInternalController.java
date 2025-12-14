package com.barofarm.support.delivery.presentation;

import com.barofarm.support.delivery.application.DeliveryService;
import com.barofarm.support.delivery.application.dto.DeliveryCreateCommand;
import com.barofarm.support.delivery.presentation.dto.internal.DeliveryInternalCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/deliveries")
public class DeliveryInternalController {

    private final DeliveryService deliveryService;

    @PostMapping
    public void createDelivery(
        @RequestBody DeliveryInternalCreateRequest request
    ) {
        deliveryService.createDelivery(
            new DeliveryCreateCommand(
                request.orderId(),
                request.address()
            )
        );
    }
}
