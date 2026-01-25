package com.barofarm.payment.payment.infrastructure.kafka.consumer;

import com.barofarm.exception.CustomException;
import com.barofarm.payment.payment.application.dto.request.TossPaymentRefundCommand;
import com.barofarm.payment.payment.domain.Payment;
import com.barofarm.payment.payment.domain.PaymentOutboxEvent;
import com.barofarm.payment.payment.domain.PaymentOutboxEventRepository;
import com.barofarm.payment.payment.domain.PaymentRepository;
import com.barofarm.payment.payment.domain.PaymentStatus;
import com.barofarm.payment.payment.exception.PaymentErrorCode;
import com.barofarm.payment.payment.infrastructure.kafka.consumer.dto.OrderCancelRequestedEvent;
import com.barofarm.payment.payment.infrastructure.kafka.producer.dto.PaymentCancelFailedEvent;
import com.barofarm.payment.payment.infrastructure.kafka.producer.dto.PaymentCanceledEvent;
import com.barofarm.payment.payment.infrastructure.rest.DepositClient;
import com.barofarm.payment.payment.infrastructure.rest.TossPaymentClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class
OrderCancelRequestedConsumer {

    private final PaymentRepository paymentRepository;
    private final TossPaymentClient tossPaymentClient;
    private final PaymentOutboxEventRepository paymentOutboxEventRepository;
    private final ObjectMapper objectMapper;
    private final DepositClient depositClient;

    @KafkaListener(
        topics = "order-cancel-requested",
        groupId = "payment-service.order-cancel-requested",
        properties = {
            "spring.json.value.default.type="
                + "com.barofarm.payment.payment.infrastructure.kafka.consumer.dto.OrderCancelRequestedEvent"
        }
    )
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Transactional
    public void handle(OrderCancelRequestedEvent event) {
        Optional<Payment> optionalPayment = paymentRepository.findByOrderId(event.orderId());
        if (optionalPayment.isEmpty()) {
            // No payment exists for this order; treat as idempotent no-op.
            return;
        }

        Payment payment = optionalPayment.get();
        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            return;
        }

        if (payment.getStatus() != PaymentStatus.CONFIRMED) {
            return;
        }

        try {
            if ("DEPOSIT".equals(payment.getMethod())) {
                depositClient.refundDeposit(
                    payment.getUserId(),
                    event.orderId(),
                    payment.getAmount()
                );
            } else {
                TossPaymentRefundCommand command = new TossPaymentRefundCommand(
                    payment.getPaymentKey(),
                    "Order canceled: " + payment.getOrderId()
                );
                tossPaymentClient.refund(command);
            }
            payment.refund();
        } catch (RuntimeException e) {
            createCancelFailureOutbox(event, payment, e);
            throw e;
        }

        PaymentCanceledEvent canceledEvent = new PaymentCanceledEvent(
            event.orderId(),
            payment.getAmount()
        );
        try {
            String payload = objectMapper.writeValueAsString(canceledEvent);

            PaymentOutboxEvent outbox = PaymentOutboxEvent.pending(
                "PAYMENT",
                payment.getId().toString(),
                "payment-canceled",
                payment.getOrderId().toString(),
                payload
            );
            paymentOutboxEventRepository.save(outbox);
        } catch (JsonProcessingException e) {
            throw new CustomException(PaymentErrorCode.OUTBOX_SERIALIZATION_FAILED);
        }
    }

    private void createCancelFailureOutbox(OrderCancelRequestedEvent event, Payment payment, Exception cause) {
        try {
            PaymentCancelFailedEvent failedEvent = new PaymentCancelFailedEvent(
                event.orderId(),
                payment.getAmount(),
                cause.getMessage()
            );

            String payload = objectMapper.writeValueAsString(failedEvent);

            PaymentOutboxEvent outboxEvent = PaymentOutboxEvent.pending(
                "PAYMENT",
                payment.getId().toString(),
                "payment-cancel-failed",
                payment.getOrderId().toString(),
                payload
            );
            paymentOutboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException ex) {
            throw new CustomException(PaymentErrorCode.OUTBOX_SERIALIZATION_FAILED);
        }
    }
}
