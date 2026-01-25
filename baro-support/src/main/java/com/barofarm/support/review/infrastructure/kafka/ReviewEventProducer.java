package com.barofarm.support.review.infrastructure.kafka;

import com.barofarm.support.review.event.ReviewEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewEventProducer {
    private final KafkaTemplate<String, ReviewEvent> kafkaTemplate;

    private static final String TOPIC = "review-events"; // 토픽 이름

    public void send(ReviewEvent event) {
        ReviewEvent.ReviewEventData data = event.getData();
        kafkaTemplate.send(TOPIC, data.getProductId().toString(), event).whenComplete((result, ex) -> {
            if (ex == null) {
                log.info(
                    "✅ [PRODUCER] Successfully sent review event to topic '{}' - Type: {}, Review ID: {}, "
                        + "Partition: {}, Offset: {}",
                    TOPIC, event.getType(), data.getReviewId(),
                    result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
            } else {
                log.error(
                    "❌ [PRODUCER] Failed to send review event to topic '{}' - Type: {}, Review ID: {}, Error: {}",
                    TOPIC, event.getType(), data.getReviewId(), ex.getMessage(), ex);
            }
        });
    }
}
