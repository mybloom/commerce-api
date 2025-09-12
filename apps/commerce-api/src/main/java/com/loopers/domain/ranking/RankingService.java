package com.loopers.domain.ranking;

import com.loopers.application.common.PagingCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@RequiredArgsConstructor
@Service
public class RankingService {

    private final RankingRepository rankingRepository;

    public Page<RankingQuery.RankingItem> retrieve(LocalDate date, PagingCondition pagingCondition) {
        Pageable pageable = pagingCondition.toPageable();
        Page<RankingQuery.RankingItem> rankingItems = rankingRepository.findAll(date, pageable);

        return rankingItems;
    }

    public Long retrieveRankByDateAndProductId(LocalDate date, Long productId) {
        return rankingRepository.findRankByDateAndProductId(date, productId);
    }
}
