package com.loopers.testcontainers;

import com.redis.testcontainers.RedisContainer;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.utility.DockerImageName;

@Configuration
public class RedisTestContainersConfig {
    //통합 테스트 시 Redis Docker 컨테이너를 자동으로 띄워, 테스트 실행 시 Redis가 Docker 컨테이너 안에서 뜨도록 해줍니다.
    //RedisContainer는 Testcontainers에서 제공하는 Redis용 컨테이너 클래스.
    //"redis:latest" 이미지를 기반으로 Redis 컨테이너를 준비합니다.
    //테스트 실행 시 단 한 번 생성되고 재사용됨.
    private static final RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:latest"));

    //클래스가 처음 로딩될 때 실행
    static {
        //Docker로 Redis 컨테이너를 실제 실행.
        redisContainer.start();

        //실행된 Redis 컨테이너의 접속 정보(host, port 등)를 JVM 시스템 프로퍼티에 주입
        System.setProperty("datasource.redis.database", "0");
        System.setProperty("datasource.redis.master.host", redisContainer.getHost());
        System.setProperty("datasource.redis.master.port", String.valueOf(redisContainer.getFirstMappedPort()));
        System.setProperty("datasource.redis.replicas[0].host", redisContainer.getHost());
        System.setProperty("datasource.redis.replicas[0].port", String.valueOf(redisContainer.getFirstMappedPort()));
    }


}
