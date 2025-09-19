package com.loopers.job.weeklymetrics;


import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class WeeklyMetricsScheduler {

    private final JobLauncher jobLauncher;
    private final Job weeklyMetricsJob;

    @Scheduled(cron = "0 30 * * * *") // 매시 10분
    public void runJob() throws Exception {
        String toDate = LocalDate.of(2025, 7, 9).toString();

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("toDate", toDate)    // 필수 : job Parameter 설정
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(weeklyMetricsJob, jobParameters);
    }
}
