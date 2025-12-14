package com.barofarm.order.order.presentation;

import com.barofarm.order.common.response.CustomPage;
import com.barofarm.order.common.response.ResponseDto;
import com.barofarm.order.order.application.OrderService;
import com.barofarm.order.order.application.dto.response.OrderCancelInfo;
import com.barofarm.order.order.application.dto.response.OrderCreateInfo;
import com.barofarm.order.order.application.dto.response.OrderDetailInfo;
import com.barofarm.order.order.application.dto.response.OrderItemSettlementResponse;
import com.barofarm.order.order.presentation.dto.OrderCreateRequest;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.v1}/orders")
@RequiredArgsConstructor
public class OrderController implements OrderSwaggerApi {

    private final OrderService orderService;

    @PostMapping
    public ResponseDto<OrderCreateInfo> createOrder(@RequestHeader("X-User-Id") UUID userId, @RequestBody OrderCreateRequest request) {
        return orderService.createOrder(userId, request.toCommand());
    }

    @GetMapping("/{orderId}")
    public ResponseDto<OrderDetailInfo> findOrderDetail(@RequestHeader("X-User-Id") UUID userId, @PathVariable UUID orderId) {
        return orderService.findOrderDetail(userId, orderId);
    }

    @GetMapping
    public ResponseDto<CustomPage<OrderDetailInfo>> findOrderList(@RequestHeader("X-User-Id") UUID userId, Pageable pageable) {
        return orderService.findOrderList(userId, pageable);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseDto<OrderCancelInfo> cancelOrder(@RequestHeader("X-User-Id") UUID userId, @PathVariable UUID orderId) {
        return orderService.cancelOrder(userId, orderId);
    }

    @GetMapping("/internal/settlements/order-items")
    public CustomPage<OrderItemSettlementResponse> getOrderItemsForSettlement(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate,
                                                                              @RequestParam int page, @RequestParam int size) {
        return orderService.findOrderItemsForSettlement(startDate, endDate, PageRequest.of(page, size));
    }
}
