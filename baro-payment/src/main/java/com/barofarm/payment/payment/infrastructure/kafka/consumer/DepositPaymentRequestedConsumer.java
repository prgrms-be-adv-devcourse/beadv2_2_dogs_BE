package com.barofarm.payment.payment.infrastructure.kafka.consumer;

import com.barofarm.exception.CustomException;
import com.barofarm.payment.payment.domain.Payment;
import com.barofarm.payment.payment.domain.PaymentOutboxEvent;
import com.barofarm.payment.payment.domain.PaymentOutboxEventRepository;
import com.barofarm.payment.payment.domain.PaymentRepository;
import com.barofarm.payment.payment.exception.PaymentErrorCode;
import com.barofarm.payment.payment.infrastructure.kafka.consumer.dto.DepositPaymentRequestedEvent;
import com.barofarm.payment.payment.infrastructure.kafka.producer.dto.DepositPaymentFailedEvent;
import com.barofarm.payment.payment.infrastructure.kafka.producer.dto.PaymentConfirmedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class DepositPaymentRequestedConsumer {

    private final PaymentRepository paymentRepository;
    private final PaymentOutboxEventRepository paymentOutboxEventRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics = "deposit-payment-requested",
        groupId = "payment-service.deposit-payment-requested",
        properties = {
            "spring.json.value.default.type="
                + "com.barofarm.payment.payment.infrastructure.kafka.consumer.dto.DepositPaymentRequestedEvent"
        }
    )
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @Transactional
    public void handle(DepositPaymentRequestedEvent event) {
        try {
            Payment payment = Payment.of(event.userId(), event.orderId(), event.amount());
            Payment saved = paymentRepository.save(payment);

            PaymentConfirmedEvent confirmedEvent = new PaymentConfirmedEvent(
                event.orderId(),
                event.amount()
            );

            String payload = objectMapper.writeValueAsString(confirmedEvent);

            PaymentOutboxEvent outboxEvent = PaymentOutboxEvent.pending(
                "PAYMENT",
                saved.getId().toString(),
                "payment-confirmed",
                saved.getOrderId().toString(),
                payload
            );

            paymentOutboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            log.error(
                "Failed to serialize PaymentConfirmedEvent for deposit payment. orderId={}",
                event.orderId(),
                e
            );
            createFailureOutbox(event, e);
            throw new CustomException(PaymentErrorCode.OUTBOX_SERIALIZATION_FAILED);
        } catch (RuntimeException e) {
            log.error(
                "Failed to process deposit-payment-requested. orderId={}",
                event.orderId(),
                e
            );
            createFailureOutbox(event, e);
            throw e;
        }
    }

    private void createFailureOutbox(DepositPaymentRequestedEvent event, Exception cause) {
        try {
            DepositPaymentFailedEvent failedEvent = new DepositPaymentFailedEvent(
                event.userId(),
                event.orderId(),
                event.amount(),
                cause.getMessage()
            );

            String payload = objectMapper.writeValueAsString(failedEvent);

            PaymentOutboxEvent outboxEvent = PaymentOutboxEvent.pending(
                "PAYMENT",
                "DEPOSIT:" + event.orderId(),
                "deposit-payment-failed",
                event.orderId().toString(),
                payload
            );

            paymentOutboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException ex) {
            log.error(
                "Failed to serialize DepositPaymentFailedEvent. orderId={}",
                event.orderId(),
                ex
            );
            throw new CustomException(PaymentErrorCode.OUTBOX_SERIALIZATION_FAILED);
        }
    }
}
