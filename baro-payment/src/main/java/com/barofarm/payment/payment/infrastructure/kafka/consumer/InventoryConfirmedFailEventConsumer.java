package com.barofarm.payment.payment.infrastructure.kafka.consumer;

import static com.barofarm.payment.payment.exception.PaymentErrorCode.PAYMENT_NOT_FOUND;

import com.barofarm.exception.CustomException;
import com.barofarm.payment.payment.application.dto.request.TossPaymentRefundCommand;
import com.barofarm.payment.payment.domain.Payment;
import com.barofarm.payment.payment.domain.PaymentRepository;
import com.barofarm.payment.payment.domain.PaymentStatus;
import com.barofarm.payment.payment.infrastructure.kafka.consumer.dto.InventoryConfirmedFailEvent;
import com.barofarm.payment.payment.infrastructure.rest.DepositClient;
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
    private final DepositClient depositClient;

    @KafkaListener(
        topics = "inventory-confirmed-fail",
        groupId = "payment-service.inventory-confirmed-fail",
        properties = {
            "spring.json.value.default.type="
                + "com.barofarm.payment.payment.infrastructure.kafka.consumer.dto.InventoryConfirmedFailEvent"
        }
    )
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Transactional
    public void handle(InventoryConfirmedFailEvent event) {

        Payment payment = paymentRepository.findByOrderId(event.orderId())
            .orElseThrow(() -> new CustomException(PAYMENT_NOT_FOUND));

        // 이미 환불된 결제면 재처리하지 않음 (idempotent)
        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            return;
        }

        if ("DEPOSIT".equals(payment.getMethod())) {
            // 예치금 결제 보상: 예치금 환불
            depositClient.refundDeposit(
                payment.getUserId(),
                payment.getOrderId(),
                payment.getAmount()
            );
        } else {
            // PG 결제 보상: Toss 환불
            TossPaymentRefundCommand command = new TossPaymentRefundCommand(
                payment.getPaymentKey(),
                "Inventory confirmation failed for order " + payment.getOrderId()
            );
            tossPaymentClient.refund(command);
        }

        // 환불 성공 후 결제 상태를 REFUNDED로 변경
        payment.refund();
    }
}
