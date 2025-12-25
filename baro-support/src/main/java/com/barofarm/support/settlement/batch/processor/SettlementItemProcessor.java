package com.barofarm.support.settlement.batch.processor;

import com.barofarm.support.settlement.client.OrderItemSettlementResponse;
import com.barofarm.support.settlement.domain.SettlementItem;
import com.barofarm.support.settlement.domain.SettlementStatus;
import com.barofarm.support.settlement.domain.vo.OrderInfo;
import com.barofarm.support.settlement.domain.vo.SettlementCalculationInfo;
import java.time.LocalDate;
import org.springframework.batch.item.ItemProcessor;

public class SettlementItemProcessor
    implements ItemProcessor<OrderItemSettlementResponse, SettlementItem> {

    @Override
    public SettlementItem process(OrderItemSettlementResponse item) {

        // 1. 금액 계산
        Long itemPrice = item.amount() * item.quantity();
        Long commission = itemPrice * 10 / 100;
        Long settlementAmount = itemPrice - commission;

        // 2. 정산 월 계산 (전월 1일)
        LocalDate settlementMonth = LocalDate.now()
            .minusMonths(1)
            .withDayOfMonth(1);

        // 3. 정산 상태 결정
        SettlementStatus status = (item.canceledAt() != null)
            ? SettlementStatus.CANCELED
            : SettlementStatus.NORMAL;

        // 4. OrderInfo VO 생성
        OrderInfo orderInfo = new OrderInfo(
            item.orderId(),
            item.orderItemId(),
            item.sellerId(),
            item.productId(),
            item.amount(),
            item.quantity(),
            itemPrice,
            item.orderDate(),
            item.canceledAt()
        );

        // 5. SettlementCalculationInfo VO 생성
        SettlementCalculationInfo calcInfo = new SettlementCalculationInfo(
            commission,
            settlementAmount,
            status,
            settlementMonth
        );

        // 6. SettlementItem 생성 (정적 팩토리 메서드)
        return SettlementItem.of(orderInfo, calcInfo);
    }
}
