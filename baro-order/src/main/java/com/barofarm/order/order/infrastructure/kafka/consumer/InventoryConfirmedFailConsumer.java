package com.barofarm.order.order.infrastructure.kafka.consumer;

import static com.barofarm.order.order.exception.OrderErrorCode.ORDER_NOT_FOUND;

import com.barofarm.exception.CustomException;
import com.barofarm.order.order.domain.Order;
import com.barofarm.order.order.domain.OrderRepository;
import com.barofarm.order.order.infrastructure.kafka.consumer.dto.InventoryConfirmedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
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
        // 총 시도 횟수 (최초 시도 1회 + 재시도 4회)
        attempts = "5",
        // 재시도 간격 (1000ms -> 2000ms -> 4000ms -> 8000ms 순으로 재시도 시간이 증가한다.)
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Transactional
    public void handle(InventoryConfirmedEvent event) throws JsonProcessingException {

        Order order = orderRepository.findById(event.orderId())
            .orElseThrow(() -> new CustomException(ORDER_NOT_FOUND));
        order.markFailed();
    }
}
