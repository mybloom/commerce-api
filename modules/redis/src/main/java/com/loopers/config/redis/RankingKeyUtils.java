package com.loopers.config.redis;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RankingKeyUtils {
    private static final String KEY_PREFIX = "rank:all:product:";
    private static final String MEMBER_PREFIX = "productId:";
    private static LocalDate date;


    /**
     * 주어진 날짜에 해당하는 랭킹 ZSET 키 생성
     * ex) rank:all:product:20250912
     */
    public static String generateRankingKey(LocalDate date) {
        RankingKeyUtils.date = date;
        return KEY_PREFIX + date.format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    /**
     * 주어진 상품 ID에 해당하는 멤버 문자열 생성
     * ex) productId:117
     */
    public static String generateMemberKey(Long productId) {
        return MEMBER_PREFIX + productId;
    }

    /**
     * 멤버 문자열에서 productId 추출
     * ex) productId:117 -> 117
     */
    public static Long parseProductId(String memberKey) {
        return Long.parseLong(memberKey.replace(MEMBER_PREFIX, ""));
    }
}
