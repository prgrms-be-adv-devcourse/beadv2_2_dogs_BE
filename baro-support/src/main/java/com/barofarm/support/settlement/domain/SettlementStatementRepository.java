package com.barofarm.support.settlement.domain;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface SettlementStatementRepository {
    Optional<SettlementStatement> findBySellerIdAndPeriodStartAndPeriodEnd(
        UUID sellerId,
        LocalDate periodStart,
        LocalDate periodEnd
    );
}
