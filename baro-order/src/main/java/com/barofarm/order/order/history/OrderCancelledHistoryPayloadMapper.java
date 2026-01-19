package com.barofarm.order.order.history;

import com.barofarm.log.history.mapper.HistoryPayloadMapper;
import com.barofarm.log.history.model.HistoryEventType;
import com.barofarm.log.history.model.OrderEventData;
import com.barofarm.log.history.model.OrderItemData;
import com.barofarm.order.order.domain.Order;
import com.barofarm.order.order.domain.OrderRepository;
import java.lang.reflect.Method;
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

    private UUID resolveOrderId(Object[] args) {
        if (args == null || args.length < 2) {
            return null;
        }
        return args[1] instanceof UUID id ? id : null;
    }

    private OrderEventData build(UUID orderId, Order order) {
        List<OrderItemData> items = Collections.emptyList();
        if (order != null) {
            items = order.getOrderItems().stream()
                .map(item -> OrderItemData.builder()
                    .productId(item.getProductId())
                    .productName(resolveProductName(item))
                    .quantity(Math.toIntExact(item.getQuantity()))
                    .build())
                .toList();
        }

        return OrderEventData.builder()
            .orderId(order != null ? order.getId() : orderId)
            .orderItems(items)
            .build();
    }

    private String resolveProductName(Object item) {
        // TODO: remove reflection once OrderItem has getProductName()
        try {
            Method method = item.getClass().getMethod("getProductName");
            Object value = method.invoke(item);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
