package com.barofarm.buyer.product.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.barofarm.buyer.event.ProductEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventProducer {

    private final KafkaTemplate<String, ProductEvent> kafkaTemplate;

    private static final String TOPIC = "product-events"; // 토픽 이름

    public void send(ProductEvent event) {
        log.info("📤 [PRODUCER] Sending product event to topic '{}': {}", TOPIC, event);
        kafkaTemplate.send(TOPIC, event).whenComplete((result, ex) -> {
            if (ex == null) {
                log.info(
                        "✅ [PRODUCER] Successfully sent product event to topic '{}' with offset: {}",
                        TOPIC, result.getRecordMetadata().offset());
            } else {
                log.error("❌ [PRODUCER] Failed to send product event to topic '{}': {}",
                        TOPIC, ex.getMessage());
            }
        });
    }
}
