package com.barofarm.order.order.application;

import com.barofarm.dto.ResponseDto;
import com.barofarm.order.order.application.dto.request.OrderCreateCommand;
import com.barofarm.order.order.application.dto.response.OrderCreateInfo;
import com.barofarm.order.order.infrastructure.rest.InventoryClient;
import com.barofarm.order.order.infrastructure.rest.dto.InventoryReserveRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderOrchestrator {

    private final OrderService orderService;
    private final InventoryClient inventoryClient;

    public ResponseDto<OrderCreateInfo> placeOrder(UUID userId, OrderCreateCommand command){

        OrderCreateInfo orderCreateInfo = orderService.createOrder(userId, command);

        try{
            InventoryReserveRequest reserveRequest = new InventoryReserveRequest(
                orderCreateInfo.orderId(),
                command.items().stream()
                    .map(i -> new InventoryReserveRequest.Item(i.productId(), i.inventoryId(), i.quantity()))
                    .toList()
            );
            inventoryClient.reserveInventory(reserveRequest);

            orderService.markAwaitingPayment(orderCreateInfo.orderId());
            return ResponseDto.ok(orderCreateInfo);
        } catch (Exception e){
            // 부분 성공 상태(주문만 생성되거나 재고만 예약) 방지
            orderService.compensateInventory(orderCreateInfo.orderId());
            throw e;
        }
    }
}
