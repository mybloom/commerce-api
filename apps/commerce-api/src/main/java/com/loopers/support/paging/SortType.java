package com.loopers.support.paging;

import org.springframework.data.domain.Sort;

public interface SortType {
    Sort toSort();
}
