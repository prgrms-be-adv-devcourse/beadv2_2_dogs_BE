package com.barofarm.order.order.client;

import com.barofarm.order.order.config.InventoryFeignConfig;
import com.barofarm.order.order.presentation.dto.InventoryDecreaseRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "inventory-service",
    url = "http://localhost:8080/api/v1/inventories",
    configuration = InventoryFeignConfig.class
)
public interface InventoryClient {

    @PostMapping("/decrease")
    void decreaseStock(@RequestBody InventoryDecreaseRequest request);
}
