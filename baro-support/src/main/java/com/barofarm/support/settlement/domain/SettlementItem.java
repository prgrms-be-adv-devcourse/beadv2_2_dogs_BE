package com.barofarm.support.settlement.domain;

import com.barofarm.support.settlement.domain.vo.OrderInfo;
import com.barofarm.support.settlement.domain.vo.SettlementCalculationInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "settlement_item")
public class SettlementItem {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    // 원본 주문 정보
    private UUID orderId;

    private UUID orderItemId;

    private UUID sellerId;

    private UUID productId;

    private Long amount;

    private Integer quantity;

    // 정산 금액 계산
    private Long itemPrice;        // amount * quantity

    private Long commissionAmount; // itemPrice * 10 / 100

    private Long settlementAmount; // itemPrice - commissionAmount

    private LocalDateTime orderDate;

    private LocalDateTime canceledAt;

    @Enumerated(EnumType.STRING)
    private SettlementStatus status; // NORMAL / CANCELED

    private LocalDate settlementMonth; // 2025-01

    private SettlementItem(
        UUID settlementItemId,
        OrderInfo orderInfo,
        SettlementCalculationInfo calcInfo
    ) {
        this.id = settlementItemId;

        this.orderId = orderInfo.orderId();
        this.orderItemId = orderInfo.orderItemId();
        this.sellerId = orderInfo.sellerId();
        this.productId = orderInfo.productId();

        this.amount = orderInfo.amount();
        this.quantity = orderInfo.quantity();
        this.itemPrice = orderInfo.itemPrice();

        this.orderDate = orderInfo.orderDate();
        this.canceledAt = orderInfo.canceledAt();

        this.commissionAmount = calcInfo.commissionAmount();
        this.settlementAmount = calcInfo.settlementAmount();
        this.status = calcInfo.status();
        this.settlementMonth = calcInfo.settlementMonth();
    }

    public static SettlementItem of(
        OrderInfo orderInfo,
        SettlementCalculationInfo calcInfo
    ) {
        return new SettlementItem(UUID.randomUUID(), orderInfo, calcInfo);
    }
}
