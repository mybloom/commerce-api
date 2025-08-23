package com.loopers.infrastructure.http;

import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientTimeoutConfig {
    @Bean
    public Request.Options feignOptions() {

        int connectTimeoutMillis = 1000; // 연결 타임아웃 (ms)
        int readTimeoutMillis = 3000; //응답 타임아웃 (ms)
        return new Request.Options(connectTimeoutMillis, readTimeoutMillis);
    }
}
