# 이커머스 API 프로젝트
멀티모듈로 되어 있는 Spring Boot 기반의 이커머스 API 프로젝트입니다.

## 설계문서 

- [**주요 작업 리스트**](https://github.com/mybloom/commerce-api/pulls?q=is%3Apr+is%3Aclosed)
- [프로젝트 요구사항 명세서](./docs/design/01-requirements.md)
- [서비스 시퀀스 다이어그램](./docs/design/02-sequence-diagrams.md)
- [클래스 다이어그램](./docs/design/03-class-diagrams.md)
- [ERD](./docs/design/04-erd.md)

## 📚 작업 기록

### 🔹 Concept & Test
- [TDD 테스트 코드 작성](https://devstep.tistory.com/139)

### 🔹 Architecture & Design
- [Aggregate 분리를 고민한 이유](https://devstep.tistory.com/141)

### 🔹 Spring Framework "about @Transactional"
- [Spring @Transactional 동작, 로그로 확인하기](https://devstep.tistory.com/150)
- [@Transactional 에 대한 것](https://devstep.tistory.com/143)
- [@EventListener와 @TransactionalEventListener의 차이](https://devstep.tistory.com/152)

### 🔹 Database
- [DB 인덱스와 캐시를 이용한 상품 목록 API 성능 개선 보고서](https://devstep.tistory.com/145)
- [Redis ZSET 을 이용한 랭킹 시스템 개발](https://devstep.tistory.com/157)
- [Spring에서 Redis 사용하기 (RedisTemplate)](https://devstep.tistory.com/146)

### 🔹 Batch / Messaging
- [Kafka 하나의 토픽, 작업 특성에 맞춘 컨슈머 그룹 분리](https://devstep.tistory.com/154)
- [Spring Application Event 기본 용어와 튜토리얼 코드](https://devstep.tistory.com/155)


### 🔹 회고 & WIL
- [10주간 돌아보기](https://devstep.tistory.com/159)
- [[WIL] 루퍼스_부트캠프 9주차](https://devstep.tistory.com/158)
- [[WIL] 부트캠프 8주차](https://devstep.tistory.com/156)
- [[WIL] 부트캠프 5주차](https://devstep.tistory.com/147)
- [[WIL] 부트캠프 4주차](https://devstep.tistory.com/144)
- [[WIL] 부트캠프 3주차](https://devstep.tistory.com/142)
- [[WIL] 부트캠프 1주차](https://devstep.tistory.com/140)

## Getting Started
현재 프로젝트 안정성 및 유지보수성 등을 위해 아래와 같은 장치를 운용하고 있습니다. 이에 아래 명령어를 통해 프로젝트의 기반을 설치해주세요.
### Environment
`local` 프로필로 동작할 수 있도록, 필요 인프라를 `docker-compose` 로 제공합니다.
```shell
docker-compose -f ./docker/infra-compose.yml up
```
### Monitoring
`local` 환경에서 모니터링을 할 수 있도록, `docker-compose` 를 통해 `prometheus` 와 `grafana` 를 제공합니다.

애플리케이션 실행 이후, **http://localhost:3000** 로 접속해, admin/admin 계정으로 로그인하여 확인하실 수 있습니다.
```shell
docker-compose -f ./docker/monitoring-compose.yml up
```

## About Multi-Module Project
본 프로젝트는 멀티 모듈 프로젝트로 구성되어 있습니다. 각 모듈의 위계 및 역할을 분명히 하고, 아래와 같은 규칙을 적용합니다.

- apps : 각 모듈은 실행가능한 **SpringBootApplication** 을 의미합니다.
- modules : 특정 구현이나 도메인에 의존적이지 않고, reusable 한 configuration 을 원칙으로 합니다.
- supports : logging, monitoring 과 같이 부가적인 기능을 지원하는 add-on 모듈입니다.

```
Root
├── apps ( spring-applications )
│   └── 📦 commerce-api
├── modules ( reusable-configurations )
│   └── 📦 jpa
│   └── 📦 redis
└── supports ( add-ons )
    ├── 📦 monitoring
    └── 📦 logging
```
