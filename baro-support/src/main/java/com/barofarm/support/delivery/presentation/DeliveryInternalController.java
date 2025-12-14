package com.barofarm.support.delivery.presentation;

import com.barofarm.support.delivery.application.DeliveryService;
import com.barofarm.support.delivery.application.dto.DeliveryCreateCommand;
import com.barofarm.support.delivery.application.dto.DeliveryDetailInfo;
import com.barofarm.support.delivery.presentation.dto.internal.DeliveryInternalCreateRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/orders/{orderId}")
    public DeliveryDetailInfo getByOrderId(@PathVariable UUID orderId) {
        return deliveryService.getByOrderId(orderId);
    }

    @PatchMapping("/{id}/start")
    public void startDelivery(@PathVariable UUID id) {
        deliveryService.startDelivery(id);
    }

    @PatchMapping("/{id}/complete")
    public void completeDelivery(@PathVariable UUID id) {
        deliveryService.completeDelivery(id);
    }
}
