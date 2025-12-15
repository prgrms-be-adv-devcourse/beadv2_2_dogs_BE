package com.barofarm.support.delivery.presentation;

import com.barofarm.support.common.response.ResponseDto;
import com.barofarm.support.delivery.application.DeliveryService;
import com.barofarm.support.delivery.application.dto.DeliveryDetailInfo;
import com.barofarm.support.delivery.presentation.dto.ShipDeliveryRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/${api.v1}/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PatchMapping("/{id}/ship")
    public ResponseDto<DeliveryDetailInfo> shipDelivery(
        @PathVariable UUID id,
        @RequestHeader("X-User-Id") UUID userId,
        @RequestHeader("X-User-Role") String role,
        @Valid @RequestBody ShipDeliveryRequest request
    ) {
        return ResponseDto.ok(deliveryService.ship(request.toCommand(id, userId, role)));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseDto<DeliveryDetailInfo> getByOrderId(
        @PathVariable UUID orderId
    ) {
        return ResponseDto.ok(deliveryService.getByOrderId(orderId));
    }
}
