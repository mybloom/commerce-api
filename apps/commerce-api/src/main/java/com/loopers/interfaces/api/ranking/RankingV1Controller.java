package com.loopers.interfaces.api.ranking;

import com.loopers.application.common.PagingCondition;
import com.loopers.application.ranking.RankingResult;
import com.loopers.application.ranking.RankingUseCase;
import com.loopers.interfaces.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/rankings")
@RestController
public class RankingV1Controller implements RankingV1ApiSpec{

    private final RankingUseCase rankingUseCase;

    @GetMapping
    @Override
    public ApiResponse<RankingV1Dto.ListViewResponse> retrieveRanking(
            @RequestParam(required = true) @DateTimeFormat(pattern = "yyyyMMdd") LocalDate date,
            @Valid @ModelAttribute PagingCondition pagingCondition)
    {
        RankingResult.ListView result = rankingUseCase.retrieveRanking(date, pagingCondition);
        return ApiResponse.success(RankingV1Dto.ListViewResponse.from(result));
    }
}
