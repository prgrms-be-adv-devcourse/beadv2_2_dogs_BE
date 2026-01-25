package com.barofarm.auth.infrastructure.kafka;

import com.barofarm.auth.application.event.HotlistEventMessage;
import com.barofarm.auth.application.port.HotlistEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class HotlistEventProducer implements HotlistEventPublisher {

    private final KafkaTemplate<String, HotlistEventMessage> kafkaTemplate;
    private final String topic;

    public HotlistEventProducer(
        KafkaTemplate<String, HotlistEventMessage> kafkaTemplate,
        @Value("${opa.kafka.topic:opa-hotlist-events}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(HotlistEventMessage message) {
        if (message == null || message.getSubjectId() == null || message.getSubjectId().isBlank()) {
            return;
        }
        // Use subjectId as key to keep ordering per user.
        kafkaTemplate.send(topic, message.getSubjectId(), message);
    }
}
