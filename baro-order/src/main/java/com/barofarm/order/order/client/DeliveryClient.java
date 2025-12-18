package com.barofarm.order.order.client;

import com.barofarm.order.order.application.dto.request.DeliveryInternalCreateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "support-service"
)
public interface DeliveryClient {

    @PostMapping("/internal/deliveries")
    void createDelivery(@RequestBody DeliveryInternalCreateRequest request);
}
