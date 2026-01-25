package com.barofarm.order.order.infrastructure.kafka.consumer;

import static com.barofarm.order.order.exception.OrderErrorCode.ORDER_NOT_FOUND;

import com.barofarm.exception.CustomException;
import com.barofarm.order.order.domain.Order;
import com.barofarm.order.order.domain.OrderRepository;
import com.barofarm.order.order.infrastructure.kafka.consumer.dto.InventoryConfirmedFailEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class InventoryConfirmedFailConsumer {

    private final OrderRepository orderRepository;

    @KafkaListener(
        topics = "inventory-confirmed-fail",
        groupId = "order-service.inventory-confirmed-fail",
        properties = {
            "spring.json.value.default.type="
                + "com.barofarm.order.order.infrastructure.kafka.consumer.dto.InventoryConfirmedFailEvent"
        }
    )
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Transactional
    public void handle(InventoryConfirmedFailEvent event) {
        Order order = orderRepository.findById(event.orderId())
            .orElseThrow(() -> new CustomException(ORDER_NOT_FOUND));
        order.markFailed();
    }
}
