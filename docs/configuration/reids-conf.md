# Redis-conf.md

프로젝트에서 Redis를 읽기/쓰기 분리(마스터·리플리카)로 사용하기 위한 표준 설정과 사용 가이드를 정리합니다.<br>
revision : 2031643e 의 redis 설정에 대한 문서화입니다. 
---

## TL;DR (요약)

1. 앱은 `modules:redis`에 정의된 설정을 **가져와서** 씁니다(`spring.config.import: redis.yml`).
2. 런타임에는 **두 종류의 빈**을 씁니다.

    * **기본 템플릿(@Primary)**: `ReadFrom.REPLICA_PREFERRED`에 연결 → **읽기/캐시 조회** 용도
    * **마스터 템플릿(@Qualifier)**: `ReadFrom.MASTER`에 연결 → **쓰기/즉시 일관성(read-your-write)** 용도
3. 로컬은 `docker/infra-compose.yml`로 **6379(마스터) / 6380(리드온리)** 두 노드를 띄웁니다.
4. 테스트는 test fixtures의 **Testcontainers + Flush 유틸**을 사용해 **격리/재현 가능**하게 돌립니다.
5. 직렬화는 기본 **String**(키/값/해시키/해시값 모두). JSON을 저장하려면 값 직렬화기를 교체하세요.

> ⚠️ 실제 **빈 이름/Qualifier**는 `modules:redis`의 `RedisConfig`를 기준으로 합니다. 아래 예시는 패턴을 보여주는 것이며, **프로젝트 소스의 이름을 최종 기준**으로 삼으세요.

---

## 모듈 구조 개요

```
root
├─ apps/commerce-api                  # 애플리케이션
│  └─ src/main/resources/application.yml (spring.config.import 에 redis.yml 포함)
├─ modules/redis                      # 공통 Redis 모듈
│  ├─ src/main/java/.../RedisConfig.java       # 빈 정의(커넥션팩토리/템플릿)
│  ├─ src/main/java/.../RedisProperties.java   # datasource.redis 바인딩
│  ├─ src/main/java/.../RedisNodeInfo.java     # 노드 정보(host/port)
│  ├─ src/main/resources/redis.yml             # 프로필별 기본값
│  └─ src/testFixtures/java/...                # Testcontainers/Flush 유틸
└─ docker/infra-compose.yml            # 로컬 Redis 마스터/리플리카
```

---

## 설정 파일 (redis.yml)

`modules/redis/src/main/resources/redis.yml`에서 환경별 값을 로드합니다. 애플리케이션의 `application.yml`에는 아래처럼 import만 추가하면 됩니다.

```yaml
# apps/commerce-api/src/main/resources/application.yml
spring:
  config:
    import: classpath:redis.yml
```

**구성 키**

```yaml
# modules/redis/src/main/resources/redis.yml (발췌 예시)
spring:
  data:
    redis:
      repositories:
        enabled: false  # RedisRepository 자동 구성 비활성화

datasource:
  redis:
    database: 0
    master:
      host: ${REDIS_MASTER_HOST:localhost}
      port: ${REDIS_MASTER_PORT:6379}
    replicas:
      - host: ${REDIS_REPLICA1_HOST:localhost}
        port: ${REDIS_REPLICA1_PORT:6380}

---
# local/test 등의 프로필 블록으로 오버레이 가능
```

* `datasource.redis.database`: Redis DB index
* `datasource.redis.master`: 마스터 노드(host/port)
* `datasource.redis.replicas[]`: 리플리카 목록(0개여도 동작, 있으면 읽기 분산)

---

## 빈 구성 

`RedisConfig`는 **두 개의 커넥션 팩토리**와 **두 개의 템플릿**을 노출합니다.

* **기본 커넥션 팩토리(@Primary)**: Lettuce `ReadFrom.REPLICA_PREFERRED`
* **마스터 커넥션 팩토리(@Qualifier)**: Lettuce `ReadFrom.MASTER`
* **기본 템플릿(@Primary)**: 문자열 직렬화, 기본 커넥션에 연결
* **마스터 템플릿(@Qualifier)**: 문자열 직렬화, 마스터 커넥션에 연결

> 이름 예시: `redisConnectionFactory`(기본) / `redisConnectionMaster`(마스터), `redisTemplate`(기본) / `redisTemplateMaster`(마스터). 실제 이름은 \*\*소스의 `RedisConfig`\*\*를 확인하세요.

직렬화 기본값은 `StringRedisSerializer`로 **키/값/해시키/해시값**에 동일 적용됩니다. 값에 객체(JSON)를 저장하려면 **값 직렬화기**만 교체하세요.

- [ ] 여기서 구성한 RedisTemplate을 사용하지 않는데, 사용하도록 코드 수정 
- [ ] 직렬화기를 JSON으로 바꾸려면 `RedisTemplate<String, YourType>` + `Jackson2JsonRedisSerializer<YourType>`로 교체
> 값 직렬화를 JSON으로 바꾸려면 `RedisTemplate<String, String>` 대신 `RedisTemplate<String, YourType>` + `Jackson2JsonRedisSerializer<YourType>` 구성으로 교체하세요(키 직렬화는 String 유지 권장).

---

## 사용 가이드 (언제 어떤 템플릿?)

### ✅ 기본 템플릿(리드 선호)을 쓰는 경우

* **캐시 조회**(제품 목록, 상세 캐시, 인기순 등)
* **트래픽 대부분이 읽기**이고, **즉시 일관성이 필요하지 않을 때**

```java
@RequiredArgsConstructor
@Service
public class ProductCacheReader {
    private final RedisTemplate<String, String> redis; // @Primary 주입 (리플리카 선호)

    public Optional<String> find(String key) {
        return Optional.ofNullable(redis.opsForValue().get(key));
    }
}
```

### ✅ 마스터 템플릿을 써야 하는 경우

* **쓰기 직후 즉시 읽기(read-your-write)**
* **원자적 연산/락/Lua 스크립트** 등 강한 일관성이 필요한 로직
* **카운터/포인트 차감** 등 동시성 민감 로직

```java
@RequiredArgsConstructor
@Service
public class TokenService {
    @Qualifier("redisTemplateMaster") // 실제 Qualifier 명 확인
    private final RedisTemplate<String, String> masterRedis;

    public void issue(String key, String jwt, Duration ttl) {
        masterRedis.opsForValue().set(key, jwt, ttl);
    }
}
```

> 💡 리플리카는 비동기 복제입니다. **쓰기 직후 곧바로 기본 템플릿으로 읽으면 값이 보장되지 않습니다.** 이런 경로는 반드시 마스터 템플릿을 사용하세요.


---

## 로컬 개발 (Docker Compose)

* 파일: `docker/infra-compose.yml`
* 포트: **6379(마스터), 6380(리드온리)**
* 특징: `replicaof`, `appendonly yes`, 헬스체크(PING)

```bash
# 기동
docker compose -f docker/infra-compose.yml up -d

# 상태확인
docker ps
redis-cli -p 6379 PING //master Redis 살아있는지 체크
redis-cli -p 6380 INFO replication | grep role //replica Redis가 잘 master에 붙어 있는지, 역할(role) 확인

# 종료
docker compose -f docker/infra-compose.yml down -v
```

> 로컬 기본값은 `redis.yml`의 `${REDIS_*}` 환경변수로도 제어 가능합니다.

---

## 테스트 (Test Fixtures)

* testFixtures에서 **Redis Testcontainers**를 제공 → 테스트 시 자동 기동/종료
* **Flush 유틸**로 각 테스트 간 Redis 상태를 격리

**Gradle 의존 예시(발췌)**

```kotlin
// apps/commerce-api/build.gradle.kts
dependencies {
    implementation(project(":modules:redis"))
    testFixturesImplementation(project(":modules:redis"))
}
```

**테스트 사용 예시**

```java
@SpringBootTest
class CacheServiceTest {

    @Autowired
    private RedisCleanUp redisCleanUp;

    @BeforeEach
    void setUp() {
        // 매 테스트 실행 전 Redis를 비워줌
        redisCleanUp.truncateAll();
    }
}
```

> 각 테스트에서 Redis 상태에 의존하면 플래키해집니다. 반드시 **격리**하세요.

---

## 관측/운영 팁

* **헬스체크**: `PING`, `INFO replication`, `ROLE`
* **지연 관측**: `latency latest` (로컬에서는 `latency-monitor-threshold`가 설정됨 : infra-compose.yml)
* **모니터링**: prod에서는 `MONITOR` 사용 지양(오버헤드 큼), 대신 Exporter/Agent 사용 권장
* **타임아웃/풀**: 운영 환경에서는 Lettuce 커넥션/커맨드 타임아웃, 풀 사이즈 등을 프로퍼티로 노출/설정하세요.

---
