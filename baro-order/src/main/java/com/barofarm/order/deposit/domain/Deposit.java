package com.barofarm.order.deposit.domain;

import com.barofarm.order.common.entity.BaseEntity;
import com.barofarm.order.payment.client.dto.TossPaymentResponse;
import com.barofarm.order.payment.domain.PaymentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "deposit")
public class Deposit extends BaseEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    private UUID userId;

    @Column(name = "total_amount", nullable = false)
    private Long amount;

    private Deposit(UUID id, UUID userId) {
        this.id = id;
        this.userId = userId;
        this.amount = 0L;
    }

    public static Deposit of(UUID userId) {
        return new Deposit(
            UUID.randomUUID(),
            userId
        );
    }

    public void increase(Long amount){
        this.amount += amount;
    }

    public void decrease(long amount) {
        this.amount -= amount;
    }
}
