package com.loopers.application.ranking;

import com.loopers.domain.ranking.MonthlyRankingService;
import com.loopers.domain.ranking.RankingResult;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MonthlyRankingWriter implements ItemWriter<RankingResult> {

    private final MonthlyRankingService monthlyRankingService;

    @Override
    public void write(Chunk<? extends RankingResult> chunk) {
        // MonthlyRankingService를 통해 저장 처리
        monthlyRankingService.saveAll((List<RankingResult>) chunk.getItems());
    }
}
