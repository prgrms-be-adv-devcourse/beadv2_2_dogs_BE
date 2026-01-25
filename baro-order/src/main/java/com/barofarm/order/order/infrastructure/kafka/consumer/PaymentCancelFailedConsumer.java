package com.barofarm.order.order.infrastructure.kafka.consumer;

import com.barofarm.exception.CustomException;
import com.barofarm.order.order.domain.Order;
import com.barofarm.order.order.domain.OrderRepository;
import com.barofarm.order.order.domain.OrderStatus;
import com.barofarm.order.order.exception.OrderErrorCode;
import com.barofarm.order.order.infrastructure.kafka.consumer.dto.PaymentCancelFailedEvent;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentCancelFailedConsumer {

    private final OrderRepository orderRepository;

    @KafkaListener(
        topics = "payment-cancel-failed",
        groupId = "order-service.payment-cancel-failed",
        properties = {
            "spring.json.value.default.type="
                + "com.barofarm.order.order.infrastructure.kafka.consumer.dto.PaymentCancelFailedEvent"
        }
    )
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Transactional
    public void handle(PaymentCancelFailedEvent event) {
        UUID orderId = event.orderId();

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new CustomException(OrderErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() == OrderStatus.CANCELED || order.getStatus() == OrderStatus.FAILED) {
            return;
        }

        order.markFailed();

        log.warn(
            "Payment cancel failed. orderId={}, amount={}, reason={}",
            event.orderId(),
            event.amount(),
            event.reason()
        );
    }
}
