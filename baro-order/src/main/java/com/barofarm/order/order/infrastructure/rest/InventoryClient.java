package com.barofarm.order.order.infrastructure.rest;

import com.barofarm.order.order.config.InventoryFeignConfig;
import com.barofarm.order.order.infrastructure.rest.dto.InventoryCancelRequest;
import com.barofarm.order.order.infrastructure.rest.dto.InventoryReserveRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "buyer-service",
    configuration = InventoryFeignConfig.class,
    path = "/internal/inventories"
)
public interface InventoryClient {
    @PostMapping("/reserve")
    void reserveInventory(@RequestBody InventoryReserveRequest request);

    @PostMapping("/cancel")
    void cancelInventory(@RequestBody InventoryCancelRequest request);
}
