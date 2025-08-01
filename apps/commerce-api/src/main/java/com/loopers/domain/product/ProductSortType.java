package com.loopers.domain.product;

import com.loopers.support.paging.SortType;
import org.springframework.data.domain.Sort;


//todo: 패키지 위치 도메인 or support(common)
public enum ProductSortType implements SortType {
    LATEST, //최신등록순
    PRICE_ASC, //가격오름차순
    PRICE_DESC,//가격 내림차순
    LIKES_DESC; //좋아요수

    public static final ProductSortType DEFAULT = LATEST;

    @Override
    public Sort toSort() {
        return switch (this) {
            case PRICE_ASC -> Sort.by(Sort.Direction.ASC, "price");
            case PRICE_DESC -> Sort.by(Sort.Direction.DESC, "price");
            case LIKES_DESC -> Sort.by(Sort.Direction.DESC, "likeCount");
            case LATEST -> Sort.by(Sort.Direction.DESC, "saleStartDate");
        };
    }
}
