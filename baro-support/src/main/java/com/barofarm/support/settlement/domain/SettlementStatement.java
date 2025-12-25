package com.barofarm.support.settlement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class SettlementStatement {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    private UUID sellerId;

    // 정산 기간 (전월 1~말일)
    private LocalDate periodStart;
    private LocalDate periodEnd;

    // 합산 값
    private Long totalSales;
    private Long totalCommission;
    private Long payoutAmount;      // 최종 지급 금액 = totalSales - totalCommission

    @Enumerated(EnumType.STRING)
    private StatementStatus status; // PENDING, CONFIRMED, PAID

    private LocalDateTime confirmedAt;
    private LocalDateTime paidAt;

    public void confirm() {
        this.status = StatementStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    public void markPaid() {
        this.status = StatementStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    @Builder
    public SettlementStatement(UUID sellerId,
                               LocalDate periodStart,
                               LocalDate periodEnd,
                               Long totalSales,
                               Long totalCommission,
                               Long payoutAmount,
                               StatementStatus status) {

        this.id = UUID.randomUUID();
        this.sellerId = sellerId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.totalSales = totalSales;
        this.totalCommission = totalCommission;
        this.payoutAmount = payoutAmount;
        this.status = status;
    }
}
