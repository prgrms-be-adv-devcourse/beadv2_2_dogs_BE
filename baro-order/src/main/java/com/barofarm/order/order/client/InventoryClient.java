package com.barofarm.order.order.client;

import com.barofarm.order.order.config.InventoryFeignConfig;
import com.barofarm.order.order.presentation.dto.InventoryDecreaseRequest;
import com.barofarm.order.order.presentation.dto.InventoryIncreaseRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "buyer-service",
    configuration = InventoryFeignConfig.class,
    path = "${api.v1}/inventories"
)
public interface InventoryClient {

    @PostMapping("/decrease")
    void decreaseStock(@RequestBody InventoryDecreaseRequest request);

    @PostMapping("/increase")
    void increaseStock(@RequestBody InventoryIncreaseRequest request);
}
