# 이커머스 API 프로젝트
멀티모듈로 되어 있는 Spring Boot 기반의 이커머스 API 프로젝트입니다.

## 설계문서

- <a href="https://github.com/mybloom/commerce-api/pulls?q=is%3Apr+is%3Aclosed" target="_blank" rel="noopener noreferrer"><b>주요 작업 리스트</b></a>
- <a href="./docs/design/01-requirements.md" target="_blank" rel="noopener noreferrer">프로젝트 요구사항 명세서</a>
- <a href="./docs/design/02-sequence-diagrams.md" target="_blank" rel="noopener noreferrer">서비스 시퀀스 다이어그램</a>
- <a href="./docs/design/03-class-diagrams.md" target="_blank" rel="noopener noreferrer">클래스 다이어그램</a>
- <a href="./docs/design/04-erd.md" target="_blank" rel="noopener noreferrer">ERD</a>

## 📚 작업 기록

### 🔹 Concept & Test
- <a href="https://devstep.tistory.com/139" target="_blank" rel="noopener noreferrer">TDD 테스트 코드 작성</a>

### 🔹 Architecture & Design
- <a href="https://devstep.tistory.com/141" target="_blank" rel="noopener noreferrer">Aggregate 분리를 고민한 이유</a>

### 🔹 Spring Framework "about @Transactional"
- <a href="https://devstep.tistory.com/150" target="_blank" rel="noopener noreferrer">Spring @Transactional 동작, 로그로 확인하기</a>
- <a href="https://devstep.tistory.com/143" target="_blank" rel="noopener noreferrer">@Transactional 에 대한 것</a>
- <a href="https://devstep.tistory.com/152" target="_blank" rel="noopener noreferrer">@EventListener와 @TransactionalEventListener의 차이</a>

### 🔹 Database
- <a href="https://devstep.tistory.com/145" target="_blank" rel="noopener noreferrer">DB 인덱스와 캐시를 이용한 상품 목록 API 성능 개선 보고서</a>
- <a href="https://devstep.tistory.com/157" target="_blank" rel="noopener noreferrer">Redis ZSET 을 이용한 랭킹 시스템 개발</a>
- <a href="https://devstep.tistory.com/146" target="_blank" rel="noopener noreferrer">Spring에서 Redis 사용하기 (RedisTemplate)</a>

### 🔹 Batch / Messaging
- <a href="https://devstep.tistory.com/154" target="_blank" rel="noopener noreferrer">Kafka 하나의 토픽, 작업 특성에 맞춘 컨슈머 그룹 분리</a>
- <a href="https://devstep.tistory.com/155" target="_blank" rel="noopener noreferrer">Spring Application Event 기본 용어와 튜토리얼 코드</a>

### 🔹 회고 & WIL
- <a href="https://devstep.tistory.com/159" target="_blank" rel="noopener noreferrer">10주간 돌아보기</a>
- <a href="https://devstep.tistory.com/158" target="_blank" rel="noopener noreferrer">[WIL] 루퍼스_부트캠프 9주차</a>
- <a href="https://devstep.tistory.com/156" target="_blank" rel="noopener noreferrer">[WIL] 부트캠프 8주차</a>
- <a href="https://devstep.tistory.com/147" target="_blank" rel="noopener noreferrer">[WIL] 부트캠프 5주차</a>
- <a href="https://devstep.tistory.com/144" target="_blank" rel="noopener noreferrer">[WIL] 부트캠프 4주차</a>
- <a href="https://devstep.tistory.com/142" target="_blank" rel="noopener noreferrer">[WIL] 부트캠프 3주차</a>
- <a href="https://devstep.tistory.com/140" target="_blank" rel="noopener noreferrer">[WIL] 부트캠프 1주차</a>


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
