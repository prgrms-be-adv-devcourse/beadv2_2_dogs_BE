package com.barofarm.support.settlement.batch;

import com.barofarm.support.settlement.batch.processor.SettlementItemProcessor;
import com.barofarm.support.settlement.batch.reader.OrderItemFeignReader;
import com.barofarm.support.settlement.batch.tasklet.SettlementStatementTasklet;
import com.barofarm.support.settlement.client.OrderItemSettlementResponse;
import com.barofarm.support.settlement.client.OrderSettlementClient;
import com.barofarm.support.settlement.domain.SettlementItem;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
@Slf4j
public class MonthSettlementBatchConfig{
    private final OrderSettlementClient orderSettlementClient;
    private final EntityManagerFactory emf;
    private final SettlementStatementTasklet statementTasklet;

    @Bean
    public Job monthlySettlementJob(JobRepository jobRepository,
                                    Step settlementItemStep,
                                    Step settlementStatementStep) {
        return new JobBuilder("monthlySettlementJob", jobRepository)
            .start(settlementItemStep)
            .next(settlementStatementStep)
            .build();
    }

    @Bean
    public Step settlementItemStep(
        JobRepository jobRepository,
        PlatformTransactionManager tx,
        ItemReader<OrderItemSettlementResponse> orderItemReader
    ) {
        return new StepBuilder("settlementItemStep", jobRepository)
            .<OrderItemSettlementResponse, SettlementItem>chunk(200, tx)
            .reader(orderItemReader)
            .processor(new SettlementItemProcessor())
            .writer(settlementItemWriter())
            .build();
    }

    @Bean
    public Step settlementStatementStep(JobRepository jobRepository,
                                        PlatformTransactionManager tx) {
        log.info("=== Monthly Settlement Statement Step Started ===");
        return new StepBuilder("settlementStatementStep", jobRepository)
            .tasklet(statementTasklet, tx)
            .build();
    }

    @Bean
    @StepScope
    public OrderItemFeignReader orderItemReader(
        @Value("#{jobParameters['baseDate']}") LocalDate baseDate
    ) {
        log.info("OrderItemFeignReader initialized with baseDate: {}", baseDate);
        YearMonth target = YearMonth.from(baseDate).minusMonths(1);

        return new OrderItemFeignReader(
            orderSettlementClient,
            target.atDay(1),
            target.atEndOfMonth()
        );
    }

    @Bean
    public JpaItemWriter<SettlementItem> settlementItemWriter() {
        log.info("SettlementItemWriter initialized");
        JpaItemWriter<SettlementItem> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(emf);
        return writer;
    }
}
