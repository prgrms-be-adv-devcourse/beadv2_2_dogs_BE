package com.barofarm.support.settlement.presentation;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BatchTestController {

    private final JobLauncher jobLauncher;
    private final Job monthlySettlementJob;

    @PostMapping("${api.v1}/batch/settlement")
    public String run(@RequestParam LocalDate baseDate) throws Exception {

        JobParameters params = new JobParametersBuilder()
            .addLocalDate("baseDate", baseDate)
            .addLong("runId", System.currentTimeMillis())
            .toJobParameters();

        jobLauncher.run(monthlySettlementJob, params);

        return "OK";
    }
}
