package com.barofarm.order.deposit.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "deposit_charge")
public class DepositCharge {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    private ChargeStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deposit_id", columnDefinition = "BINARY(16)", nullable = false)
    private Deposit deposit;

    private DepositCharge(UUID id, long amount, Deposit deposit) {
        this.id = id;
        this.amount = amount;
        this.status = ChargeStatus.PENDING;
        this.deposit = deposit;
    }

    public static DepositCharge of(long amount, Deposit deposit) {
        return new DepositCharge(UUID.randomUUID(), amount, deposit);
    }

    public boolean isPending() {
        return this.status == ChargeStatus.PENDING;
    }

    public boolean isSuccess() {
        return this.status == ChargeStatus.SUCCESS;
    }

    public void success() {
        this.status = ChargeStatus.SUCCESS;
    }

    public void fail(String paymentKey) {
        this.status = ChargeStatus.FAILED;
    }
}
