package com.barofarm.buyer.product.infrastructure.kafka;

import com.barofarm.buyer.event.ProductEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductEventProducer {

    private final KafkaTemplate<String, ProductEvent> kafkaTemplate;

    private static final String TOPIC = "product-events"; // 토픽 이름

    public void send(ProductEvent event) {
        kafkaTemplate.send(TOPIC, event); // product-events에 이벤트 발송
    }
}
