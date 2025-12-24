package com.barofarm.buyer.product.infrastructure.kafka;

import com.barofarm.buyer.product.event.ProductEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventProducer {

    private final KafkaTemplate<String, ProductEvent> kafkaTemplate;

    private static final String TOPIC = "product-events"; // 토픽 이름

    public void send(ProductEvent event) {
        ProductEvent.ProductEventData data = event.getData();
        log.info(
            "📤 [PRODUCER] Sending product event to topic '{}' - Type: {}, ID: {}, Name: {}, Category: {}, Price: {}",
            TOPIC, event.getType(), data.getProductId(), data.getProductName(),
            data.getProductCategory(), data.getPrice());
        kafkaTemplate.send(TOPIC, event).whenComplete((result, ex) -> {
            if (ex == null) {
                log.info(
                    "✅ [PRODUCER] Successfully sent product event to topic '{}' - Type: {}, Product ID: {}, "
                        + "Partition: {}, Offset: {}",
                    TOPIC, event.getType(), data.getProductId(),
                    result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
            } else {
                log.error(
                    "❌ [PRODUCER] Failed to send product event to topic '{}' - Type: {}, Product ID: {}, Error: {}",
                    TOPIC, event.getType(), data.getProductId(), ex.getMessage(), ex);
            }
        });
    }
}
