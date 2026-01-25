package com.barofarm.auth.infrastructure.outbox;

import com.barofarm.auth.domain.outbox.OutboxEvent;
import com.barofarm.auth.domain.outbox.OutboxStatus;
import com.barofarm.auth.infrastructure.jpa.OutboxEventJpaRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private final OutboxEventJpaRepository outboxEventRepository;
    private final KafkaTemplate<String, OutboxEventMessage> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Value("${auth.kafka.topic.user-events:user-events}")
    private String topic;

    @Value("${auth.outbox.max-attempts:5}")
    private int maxAttempts;

    @Scheduled(fixedDelayString = "${auth.outbox.publish-interval-ms:1000}")
    @Transactional
    public void publishPendingEvents() {
        // [0] 주기적으로 PENDING 이벤트를 스캔해 외부 브로커로 발행한다.
        List<OutboxEvent> pending = outboxEventRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        for (OutboxEvent event : pending) {
            try {
                OutboxEventMessage message = toMessage(event);
                // [1] 동기 전송으로 성공 여부를 확인한 뒤 상태를 갱신한다.
                kafkaTemplate.send(topic, message.getAggregateId(), message)
                    .get(5, TimeUnit.SECONDS);
                event.markPublished(LocalDateTime.now(clock));
            } catch (Exception ex) {
                // [2] 실패 시 횟수를 누적하고 한도를 넘기면 FAILED로 고정한다.
                event.markFailed(ex.getMessage(), maxAttempts);
            }
        }
    }

    private OutboxEventMessage toMessage(OutboxEvent event) {
        OutboxEventMessage message = new OutboxEventMessage();
        message.setEventId(event.getId().toString());
        message.setEventType(event.getEventType());
        message.setAggregateType(event.getAggregateType());
        message.setAggregateId(event.getAggregateId());
        message.setOccurredAt(event.getCreatedAt().toString());
        message.setPayload(parsePayload(event.getPayload()));
        return message;
    }

    private Map<String, Object> parsePayload(String payload) {
        try {
            return objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse outbox payload", ex);
        }
    }
}
