package com.barofarm.log.history.writer;

import com.barofarm.log.history.model.HistoryEnvelope;
import com.barofarm.log.history.model.HistoryEventType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import com.barofarm.config.HistoryLogProperties;
import java.util.UUID;

public class HistoryLogWriter {

    private static final Logger CART_HISTORY_LOG =
        LoggerFactory.getLogger("CART_HISTORY");

    private static final Logger ORDER_HISTORY_LOG =
        LoggerFactory.getLogger("ORDER_HISTORY");

    private static final Logger INTERNAL_LOG =
        LoggerFactory.getLogger(HistoryLogWriter.class);

    private final ObjectMapper objectMapper;
    private final KafkaTemplate kafkaTemplate;
    private final String aiHistoryTopic;
    private final String cartHistoryTopic;
    private final String orderHistoryTopic;

    public HistoryLogWriter(
        ObjectMapper objectMapper,
        KafkaTemplate<?, ?> kafkaTemplate,
        HistoryLogProperties properties
    ) {
        this.objectMapper = objectMapper;
        this.kafkaTemplate = (KafkaTemplate) kafkaTemplate;
        this.aiHistoryTopic = properties.getAiTopic();
        this.cartHistoryTopic = properties.getCartTopic();
        this.orderHistoryTopic = properties.getOrderTopic();
    }

    public void write(HistoryEventType type, HistoryEnvelope<?> envelope) {
        writeInternal(type, envelope, envelope.userId());
    }

    private void writeInternal(HistoryEventType type, Object payload, UUID userId) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            routeToFile(type, json);
            sendToKafka(type, userId, json);
        } catch (Exception e) {
            INTERNAL_LOG.warn("History write failed", e);
        }
    }

    private void routeToFile(HistoryEventType type, String json) {
        switch (type.getDomain()) {
            case CART -> CART_HISTORY_LOG.info(json);

            case ORDER -> ORDER_HISTORY_LOG.info(json);

            default ->
                INTERNAL_LOG.warn("Unhandled history event type: {}", type);
        }
    }

    private void sendToKafka(HistoryEventType type, UUID userId, String json) {
        if (userId == null) {
            INTERNAL_LOG.warn("History event skipped (no userId): {}", type);
            return;
        }

        String topic = resolveTopic(type);
        if (topic == null || topic.isBlank()) {
            INTERNAL_LOG.warn("History event skipped (topic missing): {}", type);
            return;
        }

        kafkaTemplate.send(topic, userId.toString(), json);
    }

    private String resolveTopic(HistoryEventType type) {
        return switch (type.getDomain()) {
            case CART -> cartHistoryTopic;
            case ORDER -> orderHistoryTopic;
            default -> aiHistoryTopic;
        };
    }
}
