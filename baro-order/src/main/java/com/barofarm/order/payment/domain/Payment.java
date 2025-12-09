package com.barofarm.order.payment.domain;

import com.barofarm.order.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payment")
public class Payment extends BaseEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", columnDefinition = "BINARY(16)", nullable = false)
    private Order order;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 30)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    private Payment(Order order, Long amount, PaymentMethod method) {
        this.id = UUID.randomUUID();
        this.order = order;
        this.amount = amount;
        this.method = method;
        this.status = PaymentStatus.PENDING;
    }

    public static Payment of(Order order, Long amount, PaymentMethod method) {
        return new Payment(order, amount, method);
    }

    public void success(String transactionId, LocalDateTime paidAt) {
        this.status = PaymentStatus.SUCCESS;
        this.transactionId = transactionId;
        this.paidAt = paidAt;
    }

    public void fail(String transactionId) {
        this.status = PaymentStatus.FAILED;
        this.transactionId = transactionId;
    }

    public void cancel(String transactionId) {
        this.status = PaymentStatus.CANCELED;
        this.transactionId = transactionId;
    }
}
