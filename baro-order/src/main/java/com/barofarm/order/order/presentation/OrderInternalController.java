package com.barofarm.order.order.presentation;

import com.barofarm.order.common.response.CustomPage;
import com.barofarm.order.order.application.OrderService;
import com.barofarm.order.order.application.dto.response.OrderItemInternalResponse;
import com.barofarm.order.order.application.dto.response.OrderItemSettlementResponse;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class OrderInternalController {

    private final OrderService orderService;

    @GetMapping("/internal/settlements/order-items")
    public CustomPage<OrderItemSettlementResponse> getOrderItemsForSettlement(
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate,
        @RequestParam int page,
        @RequestParam int size) {
        return orderService.findOrderItemsForSettlement(startDate, endDate, PageRequest.of(page, size));
    }

    @GetMapping("/internal/order-items/{id}")
    public OrderItemInternalResponse getOrderItem(@PathVariable("id") UUID orderItemId) {
        return orderService.getOrderItem(orderItemId);
    }
}
