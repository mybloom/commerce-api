package com.loopers.support.paging;

public record Pagination(
    long totalCount,
    int page,
    int size,
    boolean hasNext
) {

    public Pagination(long totalCount, int page, int size) {
        this(totalCount, page, size, calculateHasNext(totalCount, page, size));
    }

    private static boolean calculateHasNext(long totalCount, int page, int size) {
        long offset = page * size;
        return offset + size < totalCount;
    }
}
