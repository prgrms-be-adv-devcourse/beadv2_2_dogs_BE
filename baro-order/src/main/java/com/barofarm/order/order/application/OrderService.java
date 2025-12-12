package com.barofarm.order.order.application;

import com.barofarm.order.common.exception.CustomException;
import com.barofarm.order.common.response.CustomPage;
import com.barofarm.order.common.response.ResponseDto;
import com.barofarm.order.order.application.dto.request.OrderCreateCommand;
import com.barofarm.order.order.application.dto.response.OrderCancelInfo;
import com.barofarm.order.order.application.dto.response.OrderCreateInfo;
import com.barofarm.order.order.application.dto.response.OrderDetailInfo;
import com.barofarm.order.order.client.InventoryClient;
import com.barofarm.order.order.domain.Order;
import com.barofarm.order.order.domain.OrderRepository;
import com.barofarm.order.order.domain.OrderStatus;
import com.barofarm.order.order.presentation.dto.InventoryDecreaseRequest;
import com.barofarm.order.order.presentation.dto.InventoryIncreaseRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import static com.barofarm.order.order.exception.OrderErrorCode.ORDER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    @Transactional
    public ResponseDto<OrderCreateInfo> createOrder(UUID mockUserId, OrderCreateCommand command){
        InventoryDecreaseRequest inventoryRequest = new InventoryDecreaseRequest(
            command.items().stream()
                .map(item -> new InventoryDecreaseRequest.Item(
                    item.productId(),
                    item.quantity()
                ))
                .toList()
        );
        inventoryClient.decreaseStock(inventoryRequest);

        Order order = Order.of(mockUserId, command);

        for (OrderCreateCommand.OrderItemCreateCommand item : command.items()) {
            order.addOrderItem(
                item.productId(),
                item.quantity(),
                item.unitPrice()
            );
        }

        Order saved = orderRepository.save(order);
        return ResponseDto.ok(OrderCreateInfo.from(saved));
    }

    @Transactional(readOnly = true)
    public ResponseDto<OrderDetailInfo> findOrderDetail(UUID mockUserId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ORDER_NOT_FOUND));

        if (!order.getUserId().equals(mockUserId)) {
            throw new CustomException(ORDER_NOT_FOUND);
        }
        return ResponseDto.ok(OrderDetailInfo.from(order));
    }

    @Transactional(readOnly = true)
    public ResponseDto<CustomPage<OrderDetailInfo>> findOrderList(UUID mockUserId, Pageable pageable){
        Page<OrderDetailInfo> page = orderRepository
                .findByUserIdOrderByCreatedAtDesc(mockUserId, pageable)
                .map(OrderDetailInfo::from);
        return ResponseDto.ok(CustomPage.from(page));
    }

    @Transactional
    public ResponseDto<OrderCancelInfo> cancelOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ORDER_NOT_FOUND));

        if (order.isFinished()) {
            return ResponseDto.ok(OrderCancelInfo.from(order));
        }

        InventoryIncreaseRequest inventoryRequest = new InventoryIncreaseRequest(
                order.getOrderItems().stream()
                        .map(item -> new InventoryIncreaseRequest.Item(
                                item.getProductId(),
                                item.getQuantity()
                        ))
                        .toList()
        );
        inventoryClient.increaseStock(inventoryRequest);

        order.cancel();
        return ResponseDto.ok(OrderCancelInfo.from(order));
    }

    @Transactional
    public void markOrderPaid(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ORDER_NOT_FOUND));

        if (order.getStatus() == OrderStatus.PENDING) {
            order.markPaid();
        }
    }
}
