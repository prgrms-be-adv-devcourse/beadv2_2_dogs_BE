package com.barofarm.support.deposit.infrastructure.kafka.producer;

import com.barofarm.support.deposit.domain.DepositOutboxEvent;
import com.barofarm.support.deposit.domain.DepositOutboxStatus;
import com.barofarm.support.deposit.infrastructure.DepositOutboxEventJpaRepository;
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
public class DepositOutboxPublisher {

    private final DepositOutboxEventJpaRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 2000L)
    @Transactional
    public void publishDepositEvents() {
        List<DepositOutboxEvent> events =
            outboxRepository.findTop100ByStatusOrderByCreatedAtAsc(DepositOutboxStatus.PENDING);

        for (DepositOutboxEvent event : events) {
            try {
                kafkaTemplate.send(
                    event.getTopic(),
                    event.getCorrelationId(),
                    event.getPayload()
                );
                event.markSent();
            } catch (Exception e) {
                log.error(
                    "Failed to publish deposit outbox event. id={}, topic={}",
                    event.getId(),
                    event.getTopic(),
                    e
                );
                event.markFailed();
            }
        }
    }
}
