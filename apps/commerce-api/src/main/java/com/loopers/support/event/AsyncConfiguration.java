package com.loopers.support.event;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;


@Configuration
@EnableAsync
public class AsyncConfiguration {

    // 기본 AsyncConfigurer 구현 (선택적이지만 권장)
    @Bean
    public AsyncConfigurer asyncConfigurer() {
        return new AsyncConfigurer() {
            @Override
            public Executor getAsyncExecutor() {
                // 기본 async executor 설정
                ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
                executor.setCorePoolSize(5);
                executor.setMaxPoolSize(10);
                executor.setQueueCapacity(25);
                executor.setThreadNamePrefix("Default-Async-");
                executor.initialize();
                return executor;
            }

            @Override
            public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
                // 비동기 예외 처리
                return new SimpleAsyncUncaughtExceptionHandler();
            }
        };
    }

    // 상품 조회 전용 실행자
    @Bean("productViewExecutor")
    public Executor productViewExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);           // 기본 스레드 수
        executor.setMaxPoolSize(50);            // 최대 스레드 수
        executor.setQueueCapacity(200);         // 대기 큐 크기
        executor.setKeepAliveSeconds(60);       // 유휴 스레드 생존 시간
        executor.setThreadNamePrefix("ProductView-");

        // 큐가 가득 찬 경우 처리 정책
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 애플리케이션 종료 시 진행 중인 작업 대기
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();
        return executor;
    }

    // 커스텀 예외 처리기
    @Bean
    public AsyncUncaughtExceptionHandler customAsyncExceptionHandler() {
        return new AsyncUncaughtExceptionHandler() {
            @Override
            public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {
                System.out.println("비동기 메서드에서 예외 발생: " + method.getName());
                System.out.println("예외: " + throwable.getMessage());
                System.out.println("매개변수: " + Arrays.toString(objects));

                // 로깅, 알림 등 추가 처리
                // logger.error("Async method {} failed", method.getName(), throwable);
            }
        };
    }
}
