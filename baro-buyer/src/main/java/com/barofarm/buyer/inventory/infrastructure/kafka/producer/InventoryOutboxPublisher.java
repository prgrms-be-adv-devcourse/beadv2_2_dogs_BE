package com.barofarm.buyer.inventory.infrastructure.kafka.producer;

import com.barofarm.buyer.inventory.domain.InventoryOutboxEvent;
import com.barofarm.buyer.inventory.domain.InventoryOutboxStatus;
import com.barofarm.buyer.inventory.infrastructure.InventoryOutboxEventJpaRepository;
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
public class InventoryOutboxPublisher {

    private final InventoryOutboxEventJpaRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 2000L)
    @Transactional
    public void publishInventoryEvents() {
        List<InventoryOutboxEvent> events =
            outboxRepository.findTop100ByStatusOrderByCreatedAtAsc(InventoryOutboxStatus.PENDING);

        for (InventoryOutboxEvent event : events) {
            try {
                kafkaTemplate.send(
                    event.getTopic(),
                    event.getCorrelationId(),
                    event.getPayload()
                );
                event.markSent();
            } catch (Exception e) {
                log.error("Failed to publish inventory outbox event. id={}, topic={}",
                    event.getId(), event.getTopic(), e);
                event.markFailed();
            }
        }
    }
}
