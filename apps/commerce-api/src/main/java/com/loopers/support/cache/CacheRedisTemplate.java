package com.loopers.support.cache; // 패키지 선언: 해당 클래스의 네임스페이스

import com.fasterxml.jackson.databind.ObjectMapper; // Jackson 직렬화/역직렬화에 사용할 공용 ObjectMapper 주입
import lombok.RequiredArgsConstructor; // final 필드 기반 생성자 자동 생성
import lombok.extern.slf4j.Slf4j; // 로깅용 Lombok 어노테이션
import org.springframework.beans.factory.annotation.Qualifier; // 특정 빈 식별용 @Qualifier
import org.springframework.data.redis.core.RedisTemplate; // Spring Data Redis의 핵심 템플릿
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer; // Jackson 기반 값 직렬화기
import org.springframework.stereotype.Component; // 스프링 컴포넌트 스캔 대상 지정

import java.nio.charset.StandardCharsets; // 문자셋 상수 (ISO_8859_1 사용)
import java.time.Duration; // TTL/락 등의 기간 표현
import java.util.Objects; // null 체크 유틸
import java.util.Optional; // Optional 반환 편의
import java.util.concurrent.ConcurrentHashMap; // 멀티스레드 안전한 Map (Serializer 캐시)
import java.util.function.Supplier; // 로더 함수 타입 (캐시 미스 시 원본 조회)

import static com.loopers.config.redis.RedisConfig.REDIS_TEMPLATE_MASTER; // 마스터 템플릿 식별 상수


@Slf4j // 클래스 레벨 로거 제공
@Component // 스프링 빈 등록
@RequiredArgsConstructor // final 필드 주입을 위한 생성자 자동 생성
class CacheRedisTemplate implements CacheTemplate { // CacheTemplate 구현체 (패키지 프라이빗)

    private static final String KEY_SEP = "::"; // 키 구분자 (spring-cache와 유사한 규칙)
    private static final String NULL_MARKER_SUFFIX = "::__null"; // 널 마커 키 접미사
    private static final String LOCK_PREFIX = "lock:"; // 분산락 키 접두사
    private static final String LOCK_VAL = "1"; // 분산락 값(소유자 식별은 하지 않음)

    private static final Duration DEFAULT_LOCK_TTL = Duration.ofSeconds(5); // 락 기본 TTL (Dogpile 방지)
    private static final Duration RETRY_WAIT = Duration.ofMillis(30); // 락 획득 실패 시 재시도 대기

    // 읽기: replica-preferred (RedisConfig에서 기본 템플릿을 레플리카 우선으로 구성했다고 가정)
    private final RedisTemplate<String, String> readTemplate; // 값 직렬화기는 String (ISO_8859_1로 바이트 매핑)
    // 쓰기: master 강제 (@Qualifier로 마스터 템플릿 선택)
    private final @Qualifier(REDIS_TEMPLATE_MASTER) RedisTemplate<String, String> writeTemplate; // 쓰기 일관성 보장

    private final ObjectMapper objectMapper; // 공용 ObjectMapper (JavaTimeModule 등 전역 모듈 등록됨)

    // 타입별 serializer 캐시: 직렬화기 생성 오버헤드/GC 줄이기 위해 재사용
    private final ConcurrentHashMap<Class<?>, Jackson2JsonRedisSerializer<?>> serializerCache = new ConcurrentHashMap<>();

    @Override
    public <T> T getOrLoad(
            String cacheName, // 캐시 이름 (네임스페이스)
            String key, // 캐시 키(비즈니스 키)
            Class<T> type, // 값 타입 (역직렬화 대상)
            Supplier<T> loader, // 캐시 미스 시 원본 조회 함수
            Duration ttl, // 캐시 TTL
            Duration nullTtl // 널 마커 TTL (짧게 설정해 캐시 스톰 완화)
    ) {
        String fullKey = fullKey(cacheName, key); // 완전 키 생성: "cacheName::key"

        // 1) 캐시 히트 경로: 레플리카에서 읽기
        Optional<T> got = get(cacheName, key, type); // 내부적으로 readBytes -> deserialize
        if (got.isPresent()) return got.get(); // 값이 있으면 즉시 반환

        // 2) 널 마커 확인: 최근 미스가 기록되어 있으면 바로 null 반환 (짧은 TTL)
        if (Boolean.TRUE.equals(readTemplate.hasKey(nullMarkerKey(fullKey)))) {
            return null; // 널 마커가 레플리카에 복제되었음을 가정
        }

        String lockKey = LOCK_PREFIX + fullKey; // 분산락 키 생성
        boolean locked = false; // 락 획득 여부 플래그
        try {
            // 3) 분산 락 시도: SETNX + EX (TTL) — 반드시 마스터 대상으로 실행
            locked = Boolean.TRUE.equals(
                    writeTemplate.opsForValue().setIfAbsent(lockKey, LOCK_VAL, DEFAULT_LOCK_TTL)
            ); // true면 락 획득, false면 다른 스레드/인스턴스가 선점

            if (!locked) { // 락 미획득 시 짧게 대기 후 한번 더 조회 (Dogpile 완화)
                sleepQuietly(RETRY_WAIT); // 30ms 대기
                Optional<T> retry = get(cacheName, key, type); // 다시 레플리카에서 조회
                if (retry.isPresent()) return retry.get(); // 채워졌으면 반환
                if (Boolean.TRUE.equals(readTemplate.hasKey(nullMarkerKey(fullKey)))) return null; // 널 마커면 null
            }

            // 4) 미스 처리 경로: 원본 로더 호출
            T loaded = loader.get(); // DB/외부 API 등 호출

            if (loaded == null) { // 원본에도 없으면
                setNullMarker(fullKey, nullTtl); // 널 마커 기록 (마스터) → 짧은 기간 동안 미스 단일화
                return null; // 즉시 null 반환
            }

            put(cacheName, key, loaded, type, ttl); // 값 캐싱 (마스터 기록)
            return loaded; // 로더 결과 반환

        } catch (Exception e) { // 캐시 경로에서의 모든 예외는 기능 저하로 처리
            log.warn("Cache getOrLoad fallback: key={}, cause={}", fullKey, e.toString()); // 경고 로깅
            // 장애 허용: 캐시 실패 시에도 서비스 지속 (원본 재호출)
            return loader.get(); // 주의: 로더 부작용/비용 고려 필요
        } finally {
            if (locked) { // 락을 잡았던 경우에만 해제 시도
                try {
                    writeTemplate.delete(lockKey); // 락 키 삭제 (소유자 검증 없이 단순 삭제)
                } catch (Exception ignore) { // 삭제 실패는 무시 (TTL로 자연 만료)
                }
            }
        }
    }

    @Override
    public <T> Optional<T> get(String cacheName, String key, Class<T> type) {
        String fullKey = fullKey(cacheName, key); // 완전 키 조립
        byte[] raw = readBytes(fullKey); // 레플리카-프리퍼드 읽기 (String -> bytes 맵핑)
        if (raw == null) return Optional.empty(); // 키 없음
        try {
            Jackson2JsonRedisSerializer<T> ser = serializer(type); // 타입별 Jackson Serializer 획득/캐싱
            T value = ser.deserialize(raw); // 바이트 -> 객체 역직렬화
            return Optional.ofNullable(value); // null 가능성 반영해 Optional 반환
        } catch (Exception e) { // 역직렬화 실패 시 해당 엔트리는 무시 (데이터 포맷 변경/깨짐 등)
            log.warn("Cache deserialize failed, ignore entry. key={}, cause={}", fullKey, e.toString());
            return Optional.empty();
        }
    }

    @Override
    public void put(String cacheName, String key, Object value, Class<?> type, Duration ttl) {
        Objects.requireNonNull(value, "value must not be null"); // null 입력 방지 (널은 별도 마커 경로)
        String fullKey = fullKey(cacheName, key); // 완전 키 구성
        try {
            @SuppressWarnings("unchecked") // 캐시에서 가져온 Serializer를 안전 캐스트
            Jackson2JsonRedisSerializer<Object> ser =
                    (Jackson2JsonRedisSerializer<Object>) serializer(type); // 타입 고정 Serializer 재사용
            byte[] bytes = ser.serialize(value); // 객체 -> 바이트 직렬화
            writeBytes(fullKey, bytes, ttl); // 마스터에 쓰기 + TTL 설정
        } catch (Exception e) { // 직렬화/쓰기 실패는 무시 (소프트 실패)
            log.warn("Cache put failed (ignored). key={}, cause={}", fullKey, e.toString());
        }
    }

    @Override
    public void evict(String cacheName, String key) {
        String fullKey = fullKey(cacheName, key); // 완전 키
        try {
            // 삭제는 마스터로: 즉시 일관성 확보 (레플리카는 레플리케이션 전파)
            writeTemplate.delete(fullKey); // 값 삭제
            writeTemplate.delete(nullMarkerKey(fullKey)); // 널 마커도 함께 제거
        } catch (Exception e) { // 삭제 실패는 무시 (TTL 만료에 의존)
            log.warn("Cache evict failed (ignored). key={}, cause={}", fullKey, e.toString());
        }
    }

    // ----- 내부 유틸 -----
    private <T> Jackson2JsonRedisSerializer<T> serializer(Class<T> type) {
        @SuppressWarnings("unchecked")
        Jackson2JsonRedisSerializer<T> cached =
                (Jackson2JsonRedisSerializer<T>) serializerCache.computeIfAbsent(type, cls ->
                        new Jackson2JsonRedisSerializer<>(objectMapper, cls) // ObjectMapper + 타입 고정 Serializer
                );
        return cached; // 타입별로 하나의 Serializer 인스턴스 재사용
    }

    private String fullKey(String cacheName, String key) {
        // spring-cache 규칙과 유사: "cacheName::key"
        return cacheName + KEY_SEP + key; // 네임스페이스 분리로 키 충돌 방지
    }

    private String nullMarkerKey(String fullKey) {
        return fullKey + NULL_MARKER_SUFFIX; // 널 마커 전용 키 만들기
    }

    private void setNullMarker(String fullKey, Duration nullTtl) {
        try {
            writeTemplate.opsForValue().set(nullMarkerKey(fullKey), "1", nullTtl); // 값은 의미 없는 "1"
        } catch (Exception e) { // 마커 실패는 조용히 무시 (단기적 캐시 스톰 가능성 증가)
            log.debug("Null marker set failed (ignored). key={}, cause={}", fullKey, e.toString());
        }
    }

    private void writeBytes(String fullKey, byte[] bytes, Duration ttl) {
        try {
            // 값 직렬화 방식: 바이트를 ISO_8859_1로 1:1 매핑한 String으로 저장
            // (StringRedisSerializer를 사용할 때 Base64 오버헤드 없이 원바이트 보존)
            writeTemplate.opsForValue().set(
                    fullKey,  // 키 (String)
                    new String(bytes, StandardCharsets.ISO_8859_1), // 바이트 → 문자열 (손실 없음)
                    ttl // 만료 시간
            );
        } catch (Exception e) { // 네트워크/서버 오류 등은 디버그로만 남기고 무시
            log.debug("Redis write failed: key={}, cause={}", fullKey, e.toString());
        }
    }

    private byte[] readBytes(String fullKey) {
        try {
            String stored = readTemplate.opsForValue().get(fullKey); // 레플리카에서 문자열 읽기
            return stored != null
                    ? stored.getBytes(StandardCharsets.ISO_8859_1) // 문자열 → 바이트 (역방향 1:1 매핑)
                    : null; // 키 없음
        } catch (Exception e) { // 읽기 실패 시 조용히 무시 (소프트 실패)
            log.debug("Redis read failed: key={}, cause={}", fullKey, e.toString());
            return null;
        }
    }

    private static void sleepQuietly(Duration d) {
        try {
            Thread.sleep(d.toMillis()); // 지정 시간 대기 (InterruptedException 처리)
        } catch (InterruptedException ignored) { // 인터럽트 발생 시 현재 스레드 인터럽트 플래그 복원
            Thread.currentThread().interrupt();
        }
    }
}
