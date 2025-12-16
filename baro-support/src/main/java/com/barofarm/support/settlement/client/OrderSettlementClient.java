package com.barofarm.support.settlement.client;

import com.barofarm.support.common.response.CustomPage;
import java.time.LocalDate;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "order-service")
public interface OrderSettlementClient {

    @GetMapping("/internal/settlements/order-items")
    CustomPage<OrderItemSettlementResponse> getOrderItems(
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate,
        @RequestParam int page,
        @RequestParam int size
    );
}
