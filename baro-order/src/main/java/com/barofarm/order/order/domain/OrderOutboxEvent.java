package com.barofarm.order.order.domain;

import com.barofarm.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "order_outbox_event",
    indexes = @Index(name = "idx_order_outbox_status_created_at", columnList = "status, created_at")
)
public class OrderOutboxEvent extends BaseEntity {

    @Id
    private UUID id;

    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;   // "ORDER"

    @Column(name = "aggregate_id", nullable = false, length = 100)
    private String aggregateId;     // orderId

    @Column(name = "topic", nullable = false, length = 100)
    private String topic;           // ex) "order-confirmed"

    @Column(name = "correlation_id", nullable = false, length = 100)
    private String correlationId;   // 보통 orderId

    @Lob
    @Column(name = "payload", nullable = false, columnDefinition = "LONGTEXT")
    private String payload;         // JSON

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderOutboxStatus status;

    private OrderOutboxEvent(String aggregateType,
                             String aggregateId,
                             String topic,
                             String correlationId,
                             String payload) {
        this.id = UUID.randomUUID();
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.topic = topic;
        this.correlationId = correlationId;
        this.payload = payload;
        this.status = OrderOutboxStatus.PENDING;
    }

    public static OrderOutboxEvent pending(String aggregateType,
                                           String aggregateId,
                                           String topic,
                                           String correlationId,
                                           String payload) {
        return new OrderOutboxEvent(aggregateType, aggregateId, topic, correlationId, payload);
    }

    public void markSent() {
        this.status = OrderOutboxStatus.SENT;
    }

    public void markFailed() {
        this.status = OrderOutboxStatus.FAILED;
    }
}
