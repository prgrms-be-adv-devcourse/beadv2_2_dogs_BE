package com.barofarm.auth.domain.outbox;

import com.barofarm.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;

@Getter
@Entity
@Table(name = "outbox_event")
public class OutboxEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 64)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxStatus status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "last_error", length = 500)
    private String lastError;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    protected OutboxEvent() {
    }

    private OutboxEvent(
        String aggregateType,
        String aggregateId,
        String eventType,
        String payload
    ) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.attemptCount = 0;
    }

    public static OutboxEvent create(
        String aggregateType,
        String aggregateId,
        String eventType,
        String payload
    ) {
        // [1] 트랜잭션 내부에서 생성/저장하고, 별도 발행자가 처리한다.
        return new OutboxEvent(aggregateType, aggregateId, eventType, payload);
    }

    public void markPublished(LocalDateTime now) {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = now;
    }

    public void markFailed(String error, int maxAttempts) {
        // [2] 재시도 횟수를 누적하고, 한도를 넘기면 FAILED로 고정한다.
        this.attemptCount += 1;
        this.lastError = error;
        if (this.attemptCount >= maxAttempts) {
            this.status = OutboxStatus.FAILED;
        }
    }
}
