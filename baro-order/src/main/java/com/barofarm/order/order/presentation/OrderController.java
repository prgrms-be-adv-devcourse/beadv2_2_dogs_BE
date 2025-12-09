package com.barofarm.order.order.presentation;

import com.barofarm.order.order.application.OrderService;
import com.barofarm.order.order.application.dto.request.DirectOrderCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("${api.v1}/orders")
@RequiredArgsConstructor
// TODO: 나중에 인증/인가 붙이면 @RequestHeader("userId") UUID sellerId 사용
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseDto<> createDirectOrder(@RequestBody DirectOrderCreateRequest request){
        UUID mockUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        orderService.createDirectOrder(mockUserId, request);
    }
}
