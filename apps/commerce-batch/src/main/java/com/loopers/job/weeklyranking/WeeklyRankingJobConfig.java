package com.loopers.job.weeklyranking;

import com.loopers.application.ranking.WeeklyRankingService;
import com.loopers.domain.metrics.ProductMetrics;
import com.loopers.domain.ranking.RankingResult;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WeeklyRankingJobConfig {

    public static final int CHUNK_SIZE = 2;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final WeeklyRankingService weeklyRankingService;

    @Bean
    public Job weeklyRankingJob(Step weeklyRankingStep) {
        return new JobBuilder("weeklyRankingJob", jobRepository)
                .incrementer(new RunIdIncrementer())  // todo: 삭제 예정. 매번 다른 run.id 부여
                .start(weeklyRankingStep)
                .build();
    }

    @Bean
    public Step weeklyRankingStep(
            @Qualifier("productMetricsReader") ItemReader<ProductMetrics> reader,
            ItemProcessor<ProductMetrics, RankingResult> processor
    ) {
        return new StepBuilder("weeklyRankingStep", jobRepository)
                .<ProductMetrics, RankingResult>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader) // Factory에서 제공하는 공통 Reader 주입
                .processor(processor)
                .writer(items -> weeklyRankingService.saveAll((List<RankingResult>) items.getItems()))
                .allowStartIfComplete(true)   // todo: 임시 추가
                .build();
    }
}
