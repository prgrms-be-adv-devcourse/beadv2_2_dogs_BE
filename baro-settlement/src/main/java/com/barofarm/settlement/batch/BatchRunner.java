package com.barofarm.settlement.batch;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 배치 전용 실행기 (profile=batch, Kubernetes CronJob).
 * 월별 정산 Job 실행 후 프로세스 종료.
 */
@Component
@Profile("batch")
@RequiredArgsConstructor
@Slf4j
public class BatchRunner implements CommandLineRunner {

    private final JobLauncher jobLauncher;
    private final Job monthlySettlementJob;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting monthly settlement batch job (CronJob mode)...");
        LocalDate baseDate = LocalDate.now();

        JobParameters params = new JobParametersBuilder()
            .addLocalDate("baseDate", baseDate)
            .addLong("runId", System.currentTimeMillis())
            .toJobParameters();

        jobLauncher.run(monthlySettlementJob, params);
        log.info("Monthly settlement job completed successfully.");
    }
}
