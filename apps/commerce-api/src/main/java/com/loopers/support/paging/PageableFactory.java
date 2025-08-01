package com.loopers.support.paging;

import com.loopers.application.common.PagingCondition;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageableFactory {
    public static <S extends Enum<S> & SortType> Pageable from(
        Optional<S> optionalSortType,
        Optional<PagingCondition> optionalPagingCondition,
        S defaultSortType,
        int defaultPageSize
    ) {
        S sortType = optionalSortType.orElse(defaultSortType);
        Sort sort = sortType.toSort();

        PagingCondition pagingCondition = optionalPagingCondition.orElse(PagingCondition.defaultCondition(defaultPageSize));
        return pagingCondition.toPageable(sort);
    }
}
