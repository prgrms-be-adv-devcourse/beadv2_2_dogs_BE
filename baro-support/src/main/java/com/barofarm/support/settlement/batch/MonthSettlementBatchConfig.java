package com.barofarm.support.settlement.batch;

import com.barofarm.support.settlement.batch.processor.SettlementItemProcessor;
import com.barofarm.support.settlement.batch.reader.OrderItemFeignReader;
import com.barofarm.support.settlement.batch.tasklet.SettlementStatementTasklet;
import com.barofarm.support.settlement.client.OrderItemSettlementResponse;
import com.barofarm.support.settlement.client.OrderSettlementClient;
import com.barofarm.support.settlement.domain.SettlementItem;
import jakarta.persistence.EntityManagerFactory;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
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
    public Step settlementItemStep(JobRepository jobRepository,
                                   PlatformTransactionManager tx) {

        return new StepBuilder("settlementItemStep", jobRepository)
            .<OrderItemSettlementResponse, SettlementItem>chunk(200, tx)
            .reader(orderItemReader())
            .processor(new SettlementItemProcessor())
            .writer(settlementItemWriter())
            .build();
    }

    @Bean
    public Step settlementStatementStep(JobRepository jobRepository,
                                        PlatformTransactionManager tx) {
        return new StepBuilder("settlementStatementStep", jobRepository)
            .tasklet(statementTasklet, tx)
            .build();
    }

    @Bean
    public OrderItemFeignReader orderItemReader() {
        YearMonth target = YearMonth.now().minusMonths(1);
        return new OrderItemFeignReader(
            orderSettlementClient,
            target.atDay(1),
            target.atEndOfMonth()
        );
    }

    @Bean
    public JpaItemWriter<SettlementItem> settlementItemWriter() {
        JpaItemWriter<SettlementItem> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(emf);
        return writer;
    }
}
