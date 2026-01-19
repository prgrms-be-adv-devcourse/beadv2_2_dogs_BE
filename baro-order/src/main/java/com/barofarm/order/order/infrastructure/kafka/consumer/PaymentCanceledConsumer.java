package com.barofarm.order.order.infrastructure.kafka.consumer;


import com.barofarm.exception.CustomException;
import com.barofarm.order.order.domain.Order;
import com.barofarm.order.order.domain.OrderRepository;
import com.barofarm.order.order.domain.OrderStatus;
import com.barofarm.order.order.exception.OrderErrorCode;
import com.barofarm.order.order.infrastructure.kafka.consumer.dto.PaymentCanceledEvent;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentCanceledConsumer {

    private final OrderRepository orderRepository;

    @KafkaListener(
        topics = "payment-canceled",
        groupId = "order-service.payment-canceled",
        properties = {
            "spring.json.value.default.type="
                + "com.barofarm.order.order.infrastructure.kafka.consumer.dto.PaymentCanceledEvent"
        }
    )
    @Transactional
    public void handle(PaymentCanceledEvent event) {
        UUID orderId = event.orderId();

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new CustomException(OrderErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() == OrderStatus.CANCELED) {
            return;
        }

        // 결제 환불 완료 이벤트는 이제 주문 최종 취소 전에 재고 복원 단계에서만 사용된다.
        // 주문 최종 취소는 inventory-canceled 이벤트에서 처리한다.
    }
}
