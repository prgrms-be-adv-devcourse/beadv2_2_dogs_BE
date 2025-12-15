package com.barofarm.support.settlement.batch.tasklet;

import java.time.LocalDate;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SettlementStatementTasklet implements Tasklet {

    private final JdbcTemplate jdbc;

    @Override
    public RepeatStatus execute(StepContribution a, ChunkContext b) {

        YearMonth month = YearMonth.now().minusMonths(1);
        LocalDate start = month.atDay(1);

        String sql = """
            INSERT INTO settlement_statement
                (id, seller_id, total_sales, total_commission, payout_amount,
                 period_start, period_end, status)
            SELECT UUID(), seller_id,
                   SUM(item_price),
                   SUM(commission_amount),
                   SUM(settlement_amount),
                   ?, ?, 'PENDING'
            FROM settlement_item
            WHERE settlement_month = ?
            GROUP BY seller_id
            """;

        jdbc.update(sql, start, month.atEndOfMonth(), start);

        return RepeatStatus.FINISHED;
    }
}
