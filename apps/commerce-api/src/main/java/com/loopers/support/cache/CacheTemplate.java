package com.loopers.support.cache;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

public interface CacheTemplate {

    /**
     * 캐시에서 값을 조회하고(히트) 없으면 로더로 로드(미스) 후 캐시에 저장하여 반환합니다.
     * - 캐시 어사이드(cache-aside) 패턴
     * - loaded 값이 null이면 null 마커를 nullTtl로 짧게 캐싱(negative caching)
     *
     * @param cacheName 캐시 이름(네임스페이스). 예: "prd:list"
     * @param key       캐시 키(파라미터 조합 + 스키마 버전 등). 예: "page-0:size-20:sort-price:DESC:v1"
     * @param type      직렬화/역직렬화 대상 타입(고정 타입). 예: PagePayload.class
     * @param loader    캐시 미스 시 값을 로드하는 함수(DB/JPA/외부API 호출 등)
     * @param ttl       정상 값에 적용할 TTL(존속 시간). 예: Duration.ofSeconds(60)
     * @param nullTtl   null 결과(데이터 없음)에 적용할 짧은 TTL. 예: Duration.ofSeconds(20)
     * @param <T>       반환 타입
     * @return          캐시 혹은 로더에서 가져온 값(없으면 null)
     */
    <T> T getOrLoad(
            String cacheName, // 캐시 네임스페이스(캐시 그룹명). 스프링 캐시의 cacheName과 유사한 개념
            String key,       // 캐시 키(페이지/사이즈/정렬/필터/버전 등을 포함해 유일하게 구성)
            Class<T> type,    // 값의 구체 타입(직렬화용). Jackson2JsonRedisSerializer<T>에 사용
            Supplier<T> loader, // 캐시 미스 시 실행할 로딩 로직(DB 조회 등; 예외 발생 시 호출부 정책에 따름)
            Duration ttl,       // 정상 값 TTL(캐시에 저장할 유지 시간)
            Duration nullTtl    // null(또는 빈 결과) TTL(스탬피드 방지를 위한 짧은 캐시)
    );

    /** 캐시에서 바로 조회 (없으면 Optional.empty()) */
    <T> Optional<T> get(String cacheName, String key, Class<T> type);

    /** 캐시에 바로 저장 (기본은 Jackson2Json 직렬화 + TTL 적용) */
    void put(String cacheName, String key, Object value, Class<?> type, Duration ttl);

    /** 캐시와 null 마커 제거 */
    void evict(String cacheName, String key);
}
