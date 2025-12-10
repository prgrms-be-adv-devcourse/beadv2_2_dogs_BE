package com.barofarm.support.review.client.order;

import com.barofarm.support.review.client.order.dto.OrderItemResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "order-service",
    configuration = OrderFeignConfig.class
)
public interface OrderClient {
    @GetMapping("/internal/order-items/{id}")
    OrderItemResponse getOrderItem(@PathVariable("id") UUID orderItemId);
}
