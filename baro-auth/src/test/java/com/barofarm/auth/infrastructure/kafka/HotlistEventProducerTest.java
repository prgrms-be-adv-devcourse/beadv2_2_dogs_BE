package com.barofarm.auth.infrastructure.kafka;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.barofarm.auth.application.event.HotlistEventMessage;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

class HotlistEventProducerTest {

    @Test
    @SuppressWarnings("unchecked")
    void publishSendsMessageWithSubjectIdKey() {
        KafkaTemplate<String, HotlistEventMessage> kafkaTemplate = mock(KafkaTemplate.class);
        HotlistEventProducer producer = new HotlistEventProducer(kafkaTemplate, "opa-hotlist-events");

        HotlistEventMessage message = new HotlistEventMessage();
        message.setSubjectId("user-123");
        message.setSubjectType("user");

        producer.publish(message);

        verify(kafkaTemplate).send("opa-hotlist-events", "user-123", message);
    }

    @Test
    @SuppressWarnings("unchecked")
    void publishSkipsBlankSubjectId() {
        KafkaTemplate<String, HotlistEventMessage> kafkaTemplate = mock(KafkaTemplate.class);
        HotlistEventProducer producer = new HotlistEventProducer(kafkaTemplate, "opa-hotlist-events");

        HotlistEventMessage message = new HotlistEventMessage();
        message.setSubjectId(" ");

        producer.publish(message);

        verify(kafkaTemplate, never())
            .send(anyString(), anyString(), org.mockito.ArgumentMatchers.<HotlistEventMessage>any());
    }
}
