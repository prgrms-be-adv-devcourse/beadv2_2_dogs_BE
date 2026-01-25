package com.barofarm.order.order.application;

import static com.barofarm.order.order.exception.OrderErrorCode.ORDER_ACCESS_DENIED;
import static com.barofarm.order.order.exception.OrderErrorCode.ORDER_ITEM_NOT_FOUND;
import static com.barofarm.order.order.exception.OrderErrorCode.ORDER_NOT_CANCELABLE_STATUS;
import static com.barofarm.order.order.exception.OrderErrorCode.ORDER_NOT_FOUND;
import static com.barofarm.order.order.exception.OrderErrorCode.OUTBOX_SERIALIZATION_FAILED;

import com.barofarm.dto.CustomPage;
import com.barofarm.dto.ResponseDto;
import com.barofarm.exception.CustomException;
import com.barofarm.order.order.application.dto.request.OrderCreateCommand;
import com.barofarm.order.order.application.dto.response.OrderCancelInfo;
import com.barofarm.order.order.application.dto.response.OrderCreateInfo;
import com.barofarm.order.order.application.dto.response.OrderDetailInfo;
import com.barofarm.order.order.application.dto.response.OrderItemInternalResponse;
import com.barofarm.order.order.domain.CompensationRegistry;
import com.barofarm.order.order.domain.CompensationRegistryRepository;
import com.barofarm.order.order.domain.Order;
import com.barofarm.order.order.domain.OrderItem;
import com.barofarm.order.order.domain.OrderItemRepository;
import com.barofarm.order.order.domain.OrderOutboxEvent;
import com.barofarm.order.order.domain.OrderOutboxEventRepository;
import com.barofarm.order.order.domain.OrderRepository;
import com.barofarm.order.order.domain.OrderStatus;
import com.barofarm.order.order.infrastructure.kafka.producer.dto.OrderCancelRequestedEvent;
import com.barofarm.order.order.infrastructure.rest.InventoryClient;
import com.barofarm.order.order.infrastructure.rest.dto.InventoryCancelRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryClient inventoryClient;
    private final CompensationRegistryRepository compensationRegistryRepository;
    private final OrderOutboxEventRepository orderOutboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public OrderCreateInfo createOrder(UUID userId, OrderCreateCommand command){

        Order order = Order.of(userId, command);

        for (OrderCreateCommand.OrderItemCreateCommand item : command.items()) {
            order.addOrderItem(
                item.productId(),
                item.productName(),
                item.categoryCode(),
                item.sellerId(),
                item.quantity(),
                item.unitPrice(),
                item.inventoryId()
            );
        }

        Order saved = orderRepository.save(order);
        return OrderCreateInfo.from(saved);
    }

    public void compensateInventory(UUID orderId){
        try{
            markFailed(orderId);
            InventoryCancelRequest cancelRequest = new InventoryCancelRequest(
                orderId
            );
            inventoryClient.cancelInventory(cancelRequest);

        } catch (Exception e) {
            // 스케줄러(배치) : CompensationRegistry에 PENDING 상태로 있는 orderId에 대해 만약 inventoryReservation에
            // 재고가 RESERVED 상태면 재고를 RELEASE 그리고 order를 PENDING에서 FAILED로 변경
            compensationRegistryRepository.save(CompensationRegistry.of(orderId));
        }
    }

    @Transactional
    public void markAwaitingPayment(UUID orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new CustomException(ORDER_NOT_FOUND));

        order.markAwaitingPayment();
    }

    @Transactional
    public void markFailed(UUID orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new CustomException(ORDER_NOT_FOUND));

        order.markFailed();
    }

    @Transactional(readOnly = true)
    public ResponseDto<OrderDetailInfo> findOrderDetail(UUID userId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ORDER_NOT_FOUND));

        validateOwner(userId, order);
        return ResponseDto.ok(OrderDetailInfo.from(order));
    }

    @Transactional(readOnly = true)
    public ResponseDto<CustomPage<OrderDetailInfo>> findOrderList(UUID userId, Pageable pageable){
        Page<OrderDetailInfo> page = orderRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(OrderDetailInfo::from);
        return ResponseDto.ok(CustomPage.from(page));
    }

    @Transactional
    public ResponseDto<OrderCancelInfo> cancelOrder(UUID userId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ORDER_NOT_FOUND));

        validateOwner(userId, order);

        if (order.getStatus().isCanceled() || order.getStatus() == OrderStatus.CANCEL_PENDING) {
            return ResponseDto.ok(OrderCancelInfo.from(order));
        }

        if (!order.getStatus().isCancelable()) {
            throw new CustomException(ORDER_NOT_CANCELABLE_STATUS);
        }
        order.markCancelPending();

        OrderCancelRequestedEvent event = new OrderCancelRequestedEvent(
            order.getId(),
            order.getTotalAmount()
        );
        try {
            String payload = objectMapper.writeValueAsString(event);
            OrderOutboxEvent outbox = OrderOutboxEvent.pending(
                "ORDER",
                order.getId().toString(),
                "order-cancel-requested",
                order.getId().toString(),
                payload
            );
            orderOutboxEventRepository.save(outbox);
        } catch (JsonProcessingException e) {
            throw new CustomException(OUTBOX_SERIALIZATION_FAILED);
        }
        return ResponseDto.ok(OrderCancelInfo.from(order));
    }

    @Transactional(readOnly = true)
    public OrderItemInternalResponse getOrderItem(UUID orderItemId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new CustomException(ORDER_ITEM_NOT_FOUND));

        return OrderItemInternalResponse.from(orderItem);
    }

    private void validateOwner(UUID userId, Order order) {
        if (!order.getUserId().equals(userId)) {
            throw new CustomException(ORDER_ACCESS_DENIED);
        }
    }
}
