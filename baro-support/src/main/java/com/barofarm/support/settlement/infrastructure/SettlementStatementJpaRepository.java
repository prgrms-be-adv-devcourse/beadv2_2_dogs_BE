package com.barofarm.support.settlement.infrastructure;

import com.barofarm.support.settlement.domain.SettlementStatement;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementStatementJpaRepository extends JpaRepository<SettlementStatement, UUID> {

    Optional<SettlementStatement> findBySellerIdAndPeriodStartAndPeriodEnd(
        UUID sellerId,
        LocalDate periodStart,
        LocalDate periodEnd
    );
}
