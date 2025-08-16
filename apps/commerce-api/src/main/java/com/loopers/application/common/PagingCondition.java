package com.loopers.application.common;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

//todo: 클라이언트로부터 직접 전달받는 입력값에 해당하므로 application 레이어에 둠.
public record PagingCondition(
    int page,
    int size
) {
    public static final int FIRST_PAGE = 0;

    public static PagingCondition defaultCondition(int defaultSize) {
        return new PagingCondition(FIRST_PAGE, defaultSize);
    }
    public static PagingCondition create(int page, int size) {
        if (page < 0 || size <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "Invalid paging condition");
        }
        return new PagingCondition(page, size);
    }

    public int getOffset() {
        return page * size;
    }

    public Pageable toPageable(Sort sort) {
        return PageRequest.of(page, size, sort);
    }
}
