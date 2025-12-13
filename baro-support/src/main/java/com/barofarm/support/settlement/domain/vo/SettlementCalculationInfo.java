package com.barofarm.support.settlement.domain.vo;

import com.barofarm.support.settlement.domain.SettlementStatus;
import java.time.LocalDate;

public record SettlementCalculationInfo(
    Long commissionAmount,
    Long settlementAmount,
    SettlementStatus status,
    LocalDate settlementMonth
) {}
