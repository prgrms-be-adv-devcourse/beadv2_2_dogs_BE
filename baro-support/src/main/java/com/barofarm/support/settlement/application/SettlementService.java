package com.barofarm.support.settlement.application;

import com.barofarm.support.common.exception.CustomException;
import com.barofarm.support.settlement.application.dto.SettlementResponse;
import com.barofarm.support.settlement.domain.SettlementStatement;
import com.barofarm.support.settlement.domain.SettlementStatementRepository;
import com.barofarm.support.settlement.exception.SettlementErrorCode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementStatementRepository settlementRepository;

    public SettlementResponse getSettlement(UUID sellerId) {

        YearMonth settlementMonth = YearMonth.now().minusMonths(1);
        LocalDate start = settlementMonth.atDay(1);
        LocalDate end = settlementMonth.atEndOfMonth();

        SettlementStatement statement = settlementRepository
            .findBySellerIdAndPeriodStartAndPeriodEnd(sellerId, start, end)
            .orElseThrow(() -> new CustomException(SettlementErrorCode.SETTLEMENT_NOT_FOUND));

        return SettlementResponse.from(statement);
    }
}
