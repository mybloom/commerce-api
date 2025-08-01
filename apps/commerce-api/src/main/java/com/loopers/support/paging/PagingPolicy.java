package com.loopers.support.paging;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PagingPolicy {
    PRODUCT(30),
    LIKE(20),
    ORDER(10);

    private final int defaultPageSize;
}
