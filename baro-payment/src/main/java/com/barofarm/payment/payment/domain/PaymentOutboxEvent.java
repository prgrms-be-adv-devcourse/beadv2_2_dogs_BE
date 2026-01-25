package com.barofarm.payment.payment.domain;

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
    name = "payment_outbox_event",
    indexes = {
        @Index(name = "idx_outbox_status_created_at", columnList = "status, created_at")
    }
)
public class PaymentOutboxEvent extends BaseEntity {

    @Id
    private UUID id;

    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType; // "PAYMENT"

    @Column(name = "aggregate_id", nullable = false, length = 100)
    private String aggregateId; // paymentId

    @Column(name = "topic", nullable = false, length = 100)
    private String topic;

    @Column(name = "correlation_id", nullable = false, length = 100)
    private String correlationId; // orderId

    @Lob
    @Column(name = "payload", nullable = false)
    private String payload; // JSON

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentOutboxStatus status;

    private PaymentOutboxEvent(String aggregateType,
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
        this.status = PaymentOutboxStatus.PENDING;
    }

    public static PaymentOutboxEvent pending(String aggregateType,
                                      String aggregateId,
                                      String eventType,
                                      String correlationId,
                                      String payload) {
        return new PaymentOutboxEvent(aggregateType, aggregateId, eventType, correlationId, payload);
    }

    public void markSent() {
        this.status = PaymentOutboxStatus.SENT;
    }

    public void markFailed() {
        this.status = PaymentOutboxStatus.FAILED;
    }
}
