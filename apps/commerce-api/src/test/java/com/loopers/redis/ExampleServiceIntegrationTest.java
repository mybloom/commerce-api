package com.loopers.redis;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ExampleServiceIntegrationTest {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @DisplayName("레디스 설정이 잘 되어있는지 확인하기 위해 기본 명령어 수행")
    @Test
    void test() {
        // given
        redisTemplate.opsForValue().set("test", "test");

        // when
        String value = redisTemplate.opsForValue().get("test");

        // then
        assertThat(value).isEqualTo("test");
    }
}
