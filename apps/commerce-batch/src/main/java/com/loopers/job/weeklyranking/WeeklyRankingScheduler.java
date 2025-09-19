package com.loopers.job.weeklyranking;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class WeeklyRankingScheduler {

    private final JobLauncher jobLauncher;
    private final Job weeklyRankingJob;

    /**
     * 매 시 1분(예: 10:01, 11:01, 12:01...)에 실행
     */
    @Scheduled(cron = "0 44 * * * *")
    public void runWeeklyRankingJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis()) // 매 실행마다 다른 파라미터
                    .toJobParameters();

            log.info("Starting weeklyRankingJob with parameters: {}", jobParameters);
            jobLauncher.run(weeklyRankingJob, jobParameters);
        } catch (Exception e) {
            log.error("Failed to run weeklyRankingJob", e);
        }
    }
}
