package com.barofarm.settlement.client;

import com.barofarm.dto.CustomPage;
import java.time.LocalDate;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "order-service", configuration = OrderItemFeignConfig.class)
public interface OrderSettlementClient {

    @GetMapping("/internal/settlements/order-items")
    CustomPage<OrderItemSettlementResponse> getOrderItems(
        @RequestParam("startDate") LocalDate startDate,
        @RequestParam("endDate") LocalDate endDate,
        @RequestParam("page") int page,
        @RequestParam("size") int size
    );
}
