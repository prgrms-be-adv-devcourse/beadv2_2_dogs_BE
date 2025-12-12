package com.barofarm.order.order.presentation;

import com.barofarm.order.common.response.CustomPage;
import com.barofarm.order.common.response.ResponseDto;
import com.barofarm.order.order.application.OrderService;
import com.barofarm.order.order.application.dto.response.OrderCancelInfo;
import com.barofarm.order.order.application.dto.response.OrderCreateInfo;
import com.barofarm.order.order.application.dto.response.OrderDetailInfo;
import com.barofarm.order.order.presentation.dto.OrderCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("${api.v1}/orders")
@RequiredArgsConstructor
// TODO: 나중에 인증/인가 붙이면 @RequestHeader("userId") UUID sellerId 사용
public class OrderController implements OrderSwaggerApi {

    private final OrderService orderService;

    @PostMapping
    public ResponseDto<OrderCreateInfo> createOrder(@RequestBody OrderCreateRequest request) {
        UUID mockUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        return orderService.createOrder(mockUserId, request.toCommand());
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseDto<OrderCancelInfo> cancelOrder(@PathVariable UUID orderId) {
        return orderService.cancelOrder(orderId);
    }

    @GetMapping
    public ResponseDto<CustomPage<OrderDetailInfo>> findOrderList(Pageable pageable) {
        UUID mockUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        return orderService.findOrderList(mockUserId, pageable);
    }
}
