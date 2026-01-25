package com.barofarm.payment.payment.infrastructure.kafka.producer;

import com.barofarm.payment.payment.domain.PaymentOutboxEvent;
import com.barofarm.payment.payment.domain.PaymentOutboxStatus;
import com.barofarm.payment.payment.infrastructure.PaymentOutboxEventJpaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentOutboxPublisher {

    private final PaymentOutboxEventJpaRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 2000L)
    @Transactional
    public void publishOutboxEvents() {
        List<PaymentOutboxEvent> events = outboxRepository
            .findTop100ByStatusOrderByCreatedAtAsc(PaymentOutboxStatus.PENDING);

        for (PaymentOutboxEvent event : events) {
            try {
                // PaymentService 에서 3번째 인자로 "payment-confirmed" 넣었으니
                // topic 필드에는 "payment-confirmed" 이 저장되어 있음
                kafkaTemplate.send(
                    event.getTopic(),          // ex) "payment-confirmed"
                    event.getCorrelationId(),  // key: orderId
                    event.getPayload()         // value: JSON (PaymentConfirmedEvent)
                );
                event.markSent();
            } catch (Exception e) {
                log.error("Outbox publish 실패 id={}, topic={}", event.getId(), event.getTopic(), e);
                event.markFailed();
            }
        }
    }
}
