package com.barofarm.order.payment.domain;

import com.barofarm.order.common.entity.BaseEntity;
import com.barofarm.order.payment.client.dto.TossPaymentResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payment")
public class Payment extends BaseEntity {

    @Id
    private UUID id;

    @Column(name = "payment_key", nullable = false, unique = true, length = 200)
    private String paymentKey;

    @Column(name = "order_id", nullable = false, length = 100)
    private String orderId;

    @Column(name = "total_amount", nullable = false)
    private Long amount;

    @Column(name = "method", length = 50)
    private String method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Purpose purpose;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "fail_reason")
    private String failReason;

    private Payment(String paymentKey,
                    String orderId,
                    Long amount,
                    String method,
                    Purpose purpose,
                    LocalDateTime requstedAt,
                    LocalDateTime approvedAt
    ) {
        this.id = UUID.randomUUID();
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
        this.purpose = purpose;
        this.requestedAt = requstedAt;
        this.approvedAt = approvedAt;
        this.status = PaymentStatus.CONFIRMED;
        this.failReason = null;
    }


    public static Payment of(TossPaymentResponse response, Purpose purpose) {
        return new Payment(
            response.paymentKey(),
            response.orderId(),
            response.totalAmount(),
            response.method(),
            purpose,
            response.requestedAt() != null
                ? response.requestedAt().toLocalDateTime()
                : null,
            response.approvedAt() != null
                ? response.approvedAt().toLocalDateTime()
                : null
        );
    }

    public static Payment of(UUID orderId, Long amount) {
        LocalDateTime now = LocalDateTime.now();
        return new Payment(
            "DEPOSIT:" + orderId,
            orderId.toString(),
            amount,
            "DEPOSIT",
            Purpose.ORDER_PAYMENT,
            now,
            now
        );
    }

    public void markConfirmed(String method, LocalDateTime approvedAt, LocalDateTime requestedAt) {
        this.status = PaymentStatus.CONFIRMED;
        this.method = method;
        this.approvedAt = approvedAt;
        this.requestedAt = requestedAt;
        this.failReason = null;
    }

    public void refund() {
        this.status = PaymentStatus.REFUNDED;
    }
}
