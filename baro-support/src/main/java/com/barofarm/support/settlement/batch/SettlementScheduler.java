package com.barofarm.support.settlement.batch;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementScheduler {

    private final JobLauncher jobLauncher;
    private final Job monthlySettlementJob;

    @Scheduled(cron = "${spring.task.scheduling.cron.settlement}")
    public void runMonthlySettlementJob() {
        log.info("Starting monthly settlement batch job...");
        LocalDate baseDate = LocalDate.now();  //1일 기준으로 실행

        try {
            JobParameters params = new JobParametersBuilder()
                .addLocalDate("baseDate", baseDate)
                .toJobParameters();

            jobLauncher.run(monthlySettlementJob, params);
            log.info("Monthly settlement job executed successfully.");

        } catch (Exception ex) {
            log.error("Failed to execute monthly settlement job", ex);
        }
    }
}
