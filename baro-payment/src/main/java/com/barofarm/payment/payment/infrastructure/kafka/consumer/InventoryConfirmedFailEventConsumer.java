package com.barofarm.payment.payment.infrastructure.kafka.consumer;

import static com.barofarm.payment.payment.exception.PaymentErrorCode.PAYMENT_NOT_FOUND;

import com.barofarm.exception.CustomException;
import com.barofarm.payment.payment.application.dto.request.TossPaymentRefundCommand;
import com.barofarm.payment.payment.domain.Payment;
import com.barofarm.payment.payment.domain.PaymentRepository;
import com.barofarm.payment.payment.domain.PaymentStatus;
import com.barofarm.payment.payment.infrastructure.kafka.consumer.dto.InventoryConfirmedFailEvent;
import com.barofarm.payment.payment.infrastructure.rest.TossPaymentClient;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class InventoryConfirmedFailEventConsumer {

    private final PaymentRepository paymentRepository;
    private final TossPaymentClient tossPaymentClient;

    @KafkaListener(
        topics = "inventory-confirmed-fail",
        groupId = "payment-service.inventory-confirmed-fail",
        properties = {
            "spring.json.value.default.type="
                + "com.barofarm.payment.payment.infrastructure.kafka.consumer.dto.InventoryConfirmedFailEvent"
        }
    )
    @RetryableTopic(
        // 총 시도 횟수 (최초 시도 1회 + 재시도 4회)
        attempts = "5",
        // 재시도 간격 (1000ms -> 2000ms -> 4000ms -> 8000ms 순으로 재시도 시간이 증가한다.)
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Transactional
    public void handle(InventoryConfirmedFailEvent event) {

        Payment payment = paymentRepository.findByOrderId(event.orderId())
            .orElseThrow(() -> new CustomException(PAYMENT_NOT_FOUND));

        // 이미 환불된 결제는 무시 (idempotent)
        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            return;
        }

        // PG 환불 요청
        TossPaymentRefundCommand command = new TossPaymentRefundCommand(
            payment.getPaymentKey(),
            "Inventory confirmation failed for order " + payment.getOrderId()
        );
        tossPaymentClient.refund(command);

        // 로컬 결제 상태 갱신
        payment.refund();
    }
}
