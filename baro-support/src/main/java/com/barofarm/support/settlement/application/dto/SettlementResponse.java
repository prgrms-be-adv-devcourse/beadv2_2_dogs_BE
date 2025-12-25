package com.barofarm.support.settlement.application.dto;

import com.barofarm.support.settlement.domain.SettlementStatement;
import java.time.YearMonth;
import java.util.UUID;

public record SettlementResponse(
    UUID statementId,
    YearMonth settlementMonth,

    Long totalSales,
    Long totalCommission,
    Long payoutAmount
) {
    public static SettlementResponse from(SettlementStatement statement) {
        return new SettlementResponse(
            statement.getId(),
            YearMonth.from(statement.getPeriodStart()),
            statement.getTotalSales(),
            statement.getTotalCommission(),
            statement.getPayoutAmount()
        );
    }
}
