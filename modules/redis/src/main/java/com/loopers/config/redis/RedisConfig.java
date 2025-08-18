package com.loopers.config.redis; // 이 클래스의 패키지 경로 선언

import io.lettuce.core.ReadFrom; // Lettuce 클라이언트의 읽기 라우팅 정책(enum): MASTER, REPLICA_PREFERRED 등
import org.springframework.beans.factory.annotation.Qualifier; // 같은 타입의 빈이 여럿일 때 특정 이름으로 주입할 때 사용
import org.springframework.boot.context.properties.EnableConfigurationProperties; // @ConfigurationProperties 바인딩을 활성화
import org.springframework.context.annotation.Bean; // 메서드 반환 객체를 스프링 빈으로 등록
import org.springframework.context.annotation.Configuration; // 이 클래스가 설정 클래스(빈 정의)임을 명시
import org.springframework.context.annotation.Primary; // 동일 타입 빈 중 우선(기본) 주입 대상 표시
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration; // 마스터/리플리카(정적) 토폴로지 정의
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration; // Lettuce 클라이언트 설정 빌더/객체
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory; // Lettuce 기반 Redis 커넥션 팩토리(스프링이 사용하는 커넥션 소스)
import org.springframework.data.redis.core.RedisTemplate; // Redis 명령을 추상화한 템플릿(opsForValue 등)
import org.springframework.data.redis.serializer.StringRedisSerializer; // String <-> byte[] 직렬화기

import java.util.List; // List 사용
import java.util.function.Consumer; // 빌더를 커스터마이징하기 위한 람다(Consumer) 타입

@Configuration // 이 클래스를 스프링 컨테이너에 설정 클래스로 등록
@EnableConfigurationProperties(RedisProperties.class) // RedisProperties에 선언된 프리픽스(예: datasource.redis)를 바인딩하고 빈으로 등록
public class RedisConfig{ // Redis 관련 빈들을 구성하는 설정 클래스 시작
    private static final String CONNECTION_MASTER = "redisConnectionMaster"; // 마스터 ConnectionFactory 빈 이름 상수
    public static final String REDIS_TEMPLATE_MASTER = "redisTemplateMaster"; // 마스터 템플릿 빈 이름 상수(외부에서 @Qualifier로 참조용)

    private final RedisProperties redisProperties; // yml 바인딩 결과(마스터/리플리카/DB 번호 등)를 주입받아 보관

    public RedisConfig(RedisProperties redisProperties){ // 생성자 주입
        this.redisProperties = redisProperties; // 전달된 바인딩 객체를 필드에 저장
    }

    @Primary // 동일 타입의 커넥션 팩토리가 여러 개인데, 이 빈을 기본 주입 대상으로 사용
    @Bean // 스프링 빈 등록: 기본(리드 선호) 커넥션 팩토리
    public LettuceConnectionFactory defaultRedisConnectionFactory() {
        int database = redisProperties.database(); // 사용 DB 인덱스(0..15 등) 조회
        RedisNodeInfo master = redisProperties.master(); // 마스터 노드(host, port) 정보
        List<RedisNodeInfo> replicas = redisProperties.replicas(); // 리플리카 노드 목록
        // LettuceConnectionFactory를 생성할 때, ReadFrom 설정을 통해 읽기 우선순위를 지정
        //Lettuce 클라이언트 설정을 만드는 “빌더” 객체
        return lettuceConnectionFactory(
                database, master, replicas,
                builder -> builder.readFrom(ReadFrom.REPLICA_PREFERRED) // 읽기 요청은 리플리카로 우선 라우팅(없으면 마스터로 폴백)
        );
    }

    @Qualifier(CONNECTION_MASTER) // 이 이름의 빈으로 등록(마스터 전용 커넥션 팩토리)
    @Bean // 스프링 빈 등록: 마스터 전용 커넥션 팩토리
    public LettuceConnectionFactory masterRedisConnectionFactory() {
        int database = redisProperties.database(); // DB 인덱스
        RedisNodeInfo master = redisProperties.master(); // 마스터 노드
        List<RedisNodeInfo> replicas = redisProperties.replicas(); // 리플리카 목록(토폴로지 정의엔 포함 가능)
        return lettuceConnectionFactory(
                database, master, replicas,
                builder -> builder.readFrom(ReadFrom.MASTER) // 읽기/쓰기 모두 마스터로 라우팅(즉시 일관성이 필요한 경로용)
        );
    }

    @Primary // 동일 타입 RedisTemplate이 여러 개인데, 이 템플릿을 기본으로 주입
    @Bean // 스프링 빈 등록: 기본(리드 선호 커넥션에 연결된) RedisTemplate<String, String>
    public RedisTemplate<String, String> defaultRedisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>(); // 템플릿 인스턴스 생성
        return defaultRedisTemplate(redisTemplate, lettuceConnectionFactory); // 공통 직렬화/커넥션 설정 적용 후 반환
    }

    //같은 타입의 빈이 여러 개 있을 때, 스프링이 “어떤 걸 주입할지”를 정확히 지정하는 표식
    @Qualifier(REDIS_TEMPLATE_MASTER) // 이 이름으로 빈 등록(마스터 전용 템플릿), 주입 시 @Qualifier("redisTemplateMaster") 사용
    @Bean // 스프링 빈 등록: 마스터 커넥션에 연결된 RedisTemplate<String, String>
    public RedisTemplate<String, String> masterRedisTemplate(
            @Qualifier(CONNECTION_MASTER) LettuceConnectionFactory lettuceConnectionFactory // 마스터 커넥션 팩토리를 주입
    ) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>(); // 템플릿 인스턴스 생성
        return defaultRedisTemplate(redisTemplate, lettuceConnectionFactory); // 공통 직렬화/커넥션 설정 적용 후 반환
    }

    // 커넥션 팩토리를 생성하는 내부 헬퍼: 토폴로지(마스터/리플리카) + Lettuce 클라이언트 설정 조립
    private LettuceConnectionFactory lettuceConnectionFactory(
            int database, // 사용할 Redis DB 인덱스
            RedisNodeInfo master, // 마스터 노드 정보(host/port)
            List<RedisNodeInfo> replicas, // 리플리카 노드 목록
            Consumer<LettuceClientConfiguration.LettuceClientConfigurationBuilder> customizer // 빌더 커스터마이징(예: readFrom) 람다
    ){
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder = LettuceClientConfiguration.builder(); // Lettuce 클라이언트 설정 빌더 생성
        if(customizer != null) customizer.accept(builder); // 호출 측에서 전달한 커스터마이저로 빌더 설정(예: readFrom)
        LettuceClientConfiguration clientConfig = builder.build(); // 최종 클라이언트 설정 객체 생성
        RedisStaticMasterReplicaConfiguration masterReplicaConfig =
                new RedisStaticMasterReplicaConfiguration(master.host(), master.port()); // 정적 마스터/리플리카 토폴로지 시작(마스터 지정)
        masterReplicaConfig.setDatabase(database); // 사용할 DB 인덱스 지정
        for(RedisNodeInfo r : replicas){ // 리플리카 목록 순회
            masterReplicaConfig.addNode(r.host(), r.port()); // 리플리카 노드(호스트/포트) 추가
        }
        return new LettuceConnectionFactory(masterReplicaConfig, clientConfig); // 토폴로지+클라이언트 설정으로 커넥션 팩토리 생성/반환
    }

    // RedisTemplate 공통 설정(직렬화/커넥션) 적용 헬퍼 메서드(제네릭: K=키 타입, V=값 타입)
    private <K,V> RedisTemplate<K,V> defaultRedisTemplate(
            RedisTemplate<K,V> template, // 설정 대상 템플릿
            LettuceConnectionFactory connectionFactory // 연결할 커넥션 팩토리(기본 또는 마스터)
    ){
        StringRedisSerializer s = new StringRedisSerializer(); // 문자열 직렬화기 생성(UTF-8 기반)
        template.setKeySerializer(s); // 키 직렬화: String -> byte[]
        template.setValueSerializer(s); // 값 직렬화: String -> byte[] (JSON 문자열 등을 저장할 때 적합)
        template.setHashKeySerializer(s); // 해시 키 직렬화기
        template.setHashValueSerializer(s); // 해시 값 직렬화기
        template.setConnectionFactory(connectionFactory); // 이 템플릿이 사용할 커넥션 팩토리 연결
        return template; // 스프링이 @Bean 등록 시점에 afterPropertiesSet() 호출해 템플릿 초기화까지 처리
    }
}
