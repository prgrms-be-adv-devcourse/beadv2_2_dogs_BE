package com.barofarm.order.order.infrastructure.kafka.consumer;

import static com.barofarm.order.order.exception.OrderErrorCode.OUTBOX_SERIALIZATION_FAILED;

import com.barofarm.exception.CustomException;
import com.barofarm.order.order.domain.Order;
import com.barofarm.order.order.domain.OrderOutboxEvent;
import com.barofarm.order.order.domain.OrderOutboxEventRepository;
import com.barofarm.order.order.domain.OrderRepository;
import com.barofarm.order.order.domain.OrderStatus;
import com.barofarm.order.order.exception.OrderErrorCode;
import com.barofarm.order.order.infrastructure.kafka.consumer.dto.InventoryCanceledEvent;
import com.barofarm.order.order.infrastructure.kafka.producer.dto.OrderCanceledEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class InventoryCanceledConsumer {

    private final OrderRepository orderRepository;
    private final OrderOutboxEventRepository orderOutboxEventRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics = "inventory-canceled",
        groupId = "order-service.inventory-canceled",
        properties = {
            "spring.json.value.default.type="
                + "com.barofarm.order.order.infrastructure.kafka.consumer.dto.InventoryCanceledEvent"
        }
    )
    @Transactional
    public void handle(InventoryCanceledEvent event) {
        UUID orderId = event.orderId();

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new CustomException(OrderErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() == OrderStatus.CANCELED) {
            return;
        }

        if (order.getStatus() != OrderStatus.CANCEL_PENDING) {
            return;
        }

        order.markCancel();

        try {
            OrderCanceledEvent dto = new OrderCanceledEvent(orderId, order.getTotalAmount());
            String payload = objectMapper.writeValueAsString(dto);

            OrderOutboxEvent outbox = OrderOutboxEvent.pending(
                "ORDER",
                orderId.toString(),
                "order-canceled",
                orderId.toString(),
                payload
            );
            orderOutboxEventRepository.save(outbox);
        } catch (JsonProcessingException e) {
            throw new CustomException(OUTBOX_SERIALIZATION_FAILED);
        }
    }
}
