package com.loopers.support.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static com.loopers.config.redis.RedisConfig.REDIS_TEMPLATE_MASTER;


@Slf4j
@Component
@RequiredArgsConstructor
class CacheRedisTemplate implements CacheTemplate {

    private static final String KEY_SEP = "::";
    private static final String NULL_MARKER_SUFFIX = "::__null";
    private static final String LOCK_PREFIX = "lock:";
    private static final String LOCK_VAL = "1";

    private static final Duration DEFAULT_LOCK_TTL = Duration.ofSeconds(5);
    private static final Duration RETRY_WAIT = Duration.ofMillis(30);

    // 읽기: replica-preferred (RedisConfig의 @Primary 템플릿)
    private final RedisTemplate<String, String> readTemplate;
    // 쓰기: master 강제
    private final @Qualifier(REDIS_TEMPLATE_MASTER) RedisTemplate<String, String> writeTemplate;

    private final ObjectMapper objectMapper; // 공용 OM (JavaTimeModule 등 적용)

    // 타입별 serializer 캐시
    private final ConcurrentHashMap<Class<?>, Jackson2JsonRedisSerializer<?>> serializerCache = new ConcurrentHashMap<>();

    @Override
    public <T> T getOrLoad(
            String cacheName,
            String key,
            Class<T> type,
            Supplier<T> loader,
            Duration ttl,
            Duration nullTtl
    ) {
        String fullKey = fullKey(cacheName, key);

        // 1) 캐시 히트
        Optional<T> got = get(cacheName, key, type);
        if (got.isPresent()) return got.get();

        // 2) null 마커 확인 (짧은 TTL)
        if (Boolean.TRUE.equals(readTemplate.hasKey(nullMarkerKey(fullKey)))) {
            return null;
        }

        String lockKey = LOCK_PREFIX + fullKey;
        boolean locked = false;
        try {
            // 3) 분산 락 (SETNX + TTL) — 반드시 마스터로
            locked = Boolean.TRUE.equals(
                    writeTemplate.opsForValue().setIfAbsent(lockKey, LOCK_VAL, DEFAULT_LOCK_TTL)
            );

            if (!locked) {
                sleepQuietly(RETRY_WAIT);
                Optional<T> retry = get(cacheName, key, type);
                if (retry.isPresent()) return retry.get();
                if (Boolean.TRUE.equals(readTemplate.hasKey(nullMarkerKey(fullKey)))) return null;
            }

            // 4) 미스 처리
            T loaded = loader.get();

            if (loaded == null) {
                setNullMarker(fullKey, nullTtl); // 마스터로 기록
                return null;
            }

            put(cacheName, key, loaded, type, ttl); // 마스터로 기록
            return loaded;

        } catch (Exception e) {
            log.warn("Cache getOrLoad fallback: key={}, cause={}", fullKey, e.toString());
            // 장애 허용: 캐시 경로 실패 시 로더 결과 반환
            return loader.get();
        } finally {
            if (locked) {
                try {
                    writeTemplate.delete(lockKey);
                } catch (Exception ignore) {
                }
            }
        }
    }

    @Override
    public <T> Optional<T> get(String cacheName, String key, Class<T> type) {
        String fullKey = fullKey(cacheName, key);
        byte[] raw = readBytes(fullKey); // replica-preferred
        if (raw == null) return Optional.empty();
        try {
            Jackson2JsonRedisSerializer<T> ser = serializer(type);
            T value = ser.deserialize(raw);
            return Optional.ofNullable(value);
        } catch (Exception e) {
            log.warn("Cache deserialize failed, ignore entry. key={}, cause={}", fullKey, e.toString());
            return Optional.empty();
        }
    }

    @Override
    public void put(String cacheName, String key, Object value, Class<?> type, Duration ttl) {
        Objects.requireNonNull(value, "value must not be null");
        String fullKey = fullKey(cacheName, key);
        try {
            @SuppressWarnings("unchecked")
            Jackson2JsonRedisSerializer<Object> ser =
                    (Jackson2JsonRedisSerializer<Object>) serializer(type);
            byte[] bytes = ser.serialize(value);
            writeBytes(fullKey, bytes, ttl); // MASTER write
        } catch (Exception e) {
            log.warn("Cache put failed (ignored). key={}, cause={}", fullKey, e.toString());
        }
    }

    @Override
    public void evict(String cacheName, String key) {
        String fullKey = fullKey(cacheName, key);
        try {
            // 삭제는 마스터로
            writeTemplate.delete(fullKey);
            writeTemplate.delete(nullMarkerKey(fullKey));
        } catch (Exception e) {
            log.warn("Cache evict failed (ignored). key={}, cause={}", fullKey, e.toString());
        }
    }

    // ----- 내부 유틸 -----
    private <T> Jackson2JsonRedisSerializer<T> serializer(Class<T> type) {
        @SuppressWarnings("unchecked")
        Jackson2JsonRedisSerializer<T> cached =
                (Jackson2JsonRedisSerializer<T>) serializerCache.computeIfAbsent(type, cls ->
                        new Jackson2JsonRedisSerializer<>(objectMapper, cls)
                );
        return cached;
    }

    private String fullKey(String cacheName, String key) {
        // spring-cache 규칙과 유사: "cacheName::key"
        return cacheName + KEY_SEP + key;
    }

    private String nullMarkerKey(String fullKey) {
        return fullKey + NULL_MARKER_SUFFIX;
    }

    private void setNullMarker(String fullKey, Duration nullTtl) {
        try {
            writeTemplate.opsForValue().set(nullMarkerKey(fullKey), "1", nullTtl);
        } catch (Exception e) {
            log.debug("Null marker set failed (ignored). key={}, cause={}", fullKey, e.toString());
        }
    }

    private void writeBytes(String fullKey, byte[] bytes, Duration ttl) {
        try {
            // valueOps + set(key, value, timeout)
            writeTemplate.opsForValue().set(
                    fullKey,  // key (String)
                    new String(bytes, StandardCharsets.ISO_8859_1), // 바이트를 문자열로 변환
                    ttl
            );
        } catch (Exception e) {
            log.debug("Redis write failed: key={}, cause={}", fullKey, e.toString());
        }
    }

    private byte[] readBytes(String fullKey) {
        try {
            String stored = readTemplate.opsForValue().get(fullKey);
            return stored != null
                    ? stored.getBytes(StandardCharsets.ISO_8859_1)
                    : null;
        } catch (Exception e) {
            log.debug("Redis read failed: key={}, cause={}", fullKey, e.toString());
            return null;
        }
    }

    private static void sleepQuietly(Duration d) {
        try {
            Thread.sleep(d.toMillis());
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
