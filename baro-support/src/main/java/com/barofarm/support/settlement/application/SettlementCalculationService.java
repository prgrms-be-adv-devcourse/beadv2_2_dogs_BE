package com.barofarm.support.settlement.application;

import java.time.LocalDate;
import java.time.YearMonth;
import org.springframework.stereotype.Service;

@Service
public class SettlementCalculationService {

    private static final int COMMISSION_RATE = 10; // 10%

    public long calculateItemPrice(long amount, int quantity) {
        return amount * quantity;
    }

    public long calculateCommission(long itemPrice) {
        return itemPrice * COMMISSION_RATE / 100;
    }

    public long calculateSettlement(long itemPrice) {
        return itemPrice - calculateCommission(itemPrice);
    }

    public YearMonth resolveSettlementMonth(LocalDate orderDate) {
        return YearMonth.from(orderDate);
    }
}
