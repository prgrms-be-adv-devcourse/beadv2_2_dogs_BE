package com.barofarm.support.settlement.infrastructure;

import com.barofarm.support.settlement.domain.SettlementStatement;
import com.barofarm.support.settlement.domain.SettlementStatementRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class SettlementStatementRepositoryAdapter implements SettlementStatementRepository {

    private final SettlementStatementJpaRepository settlementStatementJpaRepository;

    public SettlementStatementRepositoryAdapter(SettlementStatementJpaRepository settlementStatementJpaRepository) {
        this.settlementStatementJpaRepository = settlementStatementJpaRepository;
    }

    @Override
    public Optional<SettlementStatement> findBySellerIdAndPeriodStartAndPeriodEnd(UUID sellerId, LocalDate periodStart,
                                                                                  LocalDate periodEnd) {
        return settlementStatementJpaRepository.findBySellerIdAndPeriodStartAndPeriodEnd(sellerId,
            periodStart,
            periodEnd);
    }
}
