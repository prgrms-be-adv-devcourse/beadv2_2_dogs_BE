package com.barofarm.support.deposit.domain;

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
    name = "deposit_outbox_event",
    indexes = {
        @Index(name = "idx_deposit_outbox_status_created_at", columnList = "status, created_at")
    }
)
public class DepositOutboxEvent extends BaseEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 100)
    private String aggregateId;

    @Column(name = "topic", nullable = false, length = 100)
    private String topic;

    @Column(name = "correlation_id", nullable = false, length = 100)
    private String correlationId;

    @Lob
    @Column(name = "payload", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DepositOutboxStatus status;

    private DepositOutboxEvent(String aggregateType,
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
        this.status = DepositOutboxStatus.PENDING;
    }

    public static DepositOutboxEvent pending(String aggregateType,
                                             String aggregateId,
                                             String topic,
                                             String correlationId,
                                             String payload) {
        return new DepositOutboxEvent(aggregateType, aggregateId, topic, correlationId, payload);
    }

    public void markSent() {
        this.status = DepositOutboxStatus.SENT;
    }

    public void markFailed() {
        this.status = DepositOutboxStatus.FAILED;
    }
}
