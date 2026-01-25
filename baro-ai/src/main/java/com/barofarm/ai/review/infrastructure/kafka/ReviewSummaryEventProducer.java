package com.barofarm.ai.review.infrastructure.kafka;

import com.barofarm.ai.event.model.ReviewSummaryEvent;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewSummaryEventProducer {

    private static final String TOPIC = "review-summary-events";

    private final KafkaTemplate<String, ReviewSummaryEvent> kafkaTemplate;

    public void send(ReviewSummaryEvent event) {
        String key = toKey(event.productId());
        kafkaTemplate.send(TOPIC, key, event).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("❌ [PRODUCER] Failed to send review summary. productId={}, error={}",
                    event.productId(), ex.getMessage(), ex);
                return;
            }
            log.info("✅ [PRODUCER] Sent review summary. productId={}, sentiment={}",
                event.productId(), event.sentiment());
        });
    }

    private String toKey(UUID productId) {
        return (productId == null) ? "unknown" : productId.toString();
    }
}
