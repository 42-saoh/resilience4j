package com.example.server1;

import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4jBulkheadConfigurationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CircuitConfig {
    @Bean
    public CircuitBreakerConfig circuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(25) // 실패율 25% 이상일 경우
                .waitDurationInOpenState(Duration.ofMillis(8000)) // 8초 동안 서킷 오픈 상태 유지
                .slidingWindowSize(10) // 10개의 호출에 대한 실패율을 계산
                .minimumNumberOfCalls(3) // 3개 이상의 호출이 있어야 실패율을 계산
                .slowCallRateThreshold(50) // 느린 호출이 50% 이상이면 서킷 오픈
                .slowCallDurationThreshold(Duration.ofSeconds(1)) // 느린 호출의 기준 : 1초 이상 걸릴 경우
                .build();
    }

    @Bean
    public CircuitBreakerConfig circuitBreakerConfig2() {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(25) // 실패율 25% 이상일 경우
                .waitDurationInOpenState(Duration.ofMillis(8000)) // 8초 동안 서킷 오픈 상태 유지
                .slidingWindowSize(10) // 10개의 호출에 대한 실패율을 계산
                .minimumNumberOfCalls(3) // 3개 이상의 호출이 있어야 실패율을 계산
                .slowCallRateThreshold(50) // 느린 호출이 50% 이상이면 서킷 오픈
                .slowCallDurationThreshold(Duration.ofSeconds(2)) // 느린 호출의 기준 : 2초 이상 걸릴 경우
                .build();
    }

    @Bean
    public BulkheadConfig bulkheadConfig() {
        return BulkheadConfig.custom()
                .maxConcurrentCalls(3) // 최대 3개의 호출을 허용
                .maxWaitDuration(Duration.ofMillis(1000)) // 1000ms 동안 대기
                .build();
    }

    @Bean
    public RateLimiterConfig rateLimiterConfig() {
        return RateLimiterConfig.custom()
                .timeoutDuration(Duration.ofMillis(1000)) // 호출 권한을 얻기 위해 대기할 최대 대기 시간을 설정
                .limitRefreshPeriod(Duration.ofSeconds(50)) // 호출 권한을 갱신할 주기를 설정
                .limitForPeriod(5) // 호출 권한을 갱신할 주기 동안 허용할 호출 수를 설정
                .build();
    }
}
