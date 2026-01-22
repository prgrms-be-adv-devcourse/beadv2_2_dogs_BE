package com.barofarm.ai.event.consumer;

import com.barofarm.ai.event.model.PaymentLogEvent;
import com.barofarm.ai.log.application.LogWriteService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final LogWriteService logWriteService;

    @KafkaListener(
        topics = "payment-events",
        groupId = "ai-service-payment",
        containerFactory = "paymentEventListenerContainerFactory"
    )
    public void onMessage(PaymentLogEvent event) {
        PaymentLogEvent.PaymentEventData data = event.payload();

        log.info(
            "[PAYMENT_CONSUMER] Received payment event - Type: {}, User: {}, Payment: {}, Order: {}, Amount: {}, Purpose: {}",
            event.event(), event.userId(), data.paymentId(), data.orderId(), data.amount(), data.purpose());

        try {
            String eventType = switch (event.event()) {
                case PAYMENT_CONFIRMED -> "PAYMENT_CONFIRMED";
                case DEPOSIT_CONFIRMED -> "DEPOSIT_CONFIRMED";
                default -> "UNKNOWN";
            };

            logWriteService.savePaymentEventLog(
                event.userId(),
                data.paymentId(),
                data.orderId(),
                data.amount(),
                data.purpose(),
                eventType,
                convertToInstant(event.ts())
            );

            log.info(
                "[PAYMENT_CONSUMER] Saved payment log - User: {}, Payment: {}, Order: {}",
                event.userId(), data.paymentId(), data.orderId());
        } catch (Exception e) {
            log.error(
                "[PAYMENT_CONSUMER] Failed to process payment event - Type: {}, User: {}, Payment: {}, Error: {}",
                event.event(), event.userId(), data.paymentId(), e.getMessage(), e);
            throw e;
        }
    }

    private Instant convertToInstant(java.time.OffsetDateTime offsetDateTime) {
        return offsetDateTime.toInstant();
    }
}

