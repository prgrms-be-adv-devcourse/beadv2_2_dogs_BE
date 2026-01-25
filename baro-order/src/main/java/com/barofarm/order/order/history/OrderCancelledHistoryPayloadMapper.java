package com.barofarm.order.order.history;

import com.barofarm.log.history.mapper.HistoryPayloadMapper;
import com.barofarm.log.history.model.HistoryEventType;
import com.barofarm.log.history.model.OrderEventData;
import com.barofarm.log.history.model.OrderItemData;
import com.barofarm.order.order.domain.Order;
import com.barofarm.order.order.domain.OrderRepository;
import com.barofarm.order.order.infrastructure.kafka.consumer.dto.InventoryCanceledEvent;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class OrderCancelledHistoryPayloadMapper implements HistoryPayloadMapper {

    private final OrderRepository orderRepository;

    public OrderCancelledHistoryPayloadMapper(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public HistoryEventType supports() {
        return HistoryEventType.ORDER_CANCELLED;
    }

    @Override
    public Object payload(Object[] args, Object returnValue) {
        UUID orderId = resolveOrderId(args);
        if (orderId == null) {
            return null;
        }

        Order order = orderRepository.findById(orderId).orElse(null);
        return build(orderId, order);
    }

    @Override
    public UUID resolveUserId(Object[] args, Object returnValue) {
        UUID orderId = resolveOrderId(args);
        if (orderId == null) {
            return null;
        }
        return orderRepository.findById(orderId)
            .map(Order::getUserId)
            .orElse(null);
    }

    private UUID resolveOrderId(Object[] args) {
        if (args == null || args.length < 1) {
            return null;
        }
        Object first = args[0];
        if (first instanceof InventoryCanceledEvent event) {
            return event.orderId();
        }
        return null;
    }

    private OrderEventData build(UUID orderId, Order order) {
        List<OrderItemData> items = Collections.emptyList();
        if (order != null) {
            items = order.getOrderItems().stream()
                .map(item -> OrderItemData.builder()
                    .productId(item.getProductId())
                    .productName(item.getProductName())
                    .quantity(Math.toIntExact(item.getQuantity()))
                    .categoryCode(item.getCategoryCode())
                    .build())
                .toList();
        }

        return OrderEventData.builder()
            .orderId(order != null ? order.getId() : orderId)
            .orderItems(items)
            .build();
    }
}
