package com.loopers.domain.like;

import com.loopers.support.paging.SortType;
import org.springframework.data.domain.Sort;

public enum LikeSortType implements SortType {
    LATEST; //최신등록순

    public static final LikeSortType DEFAULT = LATEST;

    @Override
    public Sort toSort() {
        return switch (this) {
            case LATEST -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }
}
