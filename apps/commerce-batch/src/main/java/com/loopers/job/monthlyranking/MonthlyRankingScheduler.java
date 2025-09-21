package com.loopers.job.monthlyranking;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyRankingScheduler {

    private final JobLauncher jobLauncher;
    private final Job monthlyRankingJob;

    // 매시 10분에 실행
    @Scheduled(cron = "0 58 * * * *")
    public void runMonthlyRankingJob() {
        try {
            String targetDate = LocalDate.of(2025, 7, 31).toString();

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("targetDate", targetDate)
                    .addLong("timestamp", System.currentTimeMillis()) // 중복 실행 방지
                    .toJobParameters();

            log.info("Starting monthlyRankingJob with targetDate={}", targetDate);
            jobLauncher.run(monthlyRankingJob, jobParameters);
        } catch (Exception e) {
            log.error("Failed to run monthlyRankingJob", e);
        }
    }
}
