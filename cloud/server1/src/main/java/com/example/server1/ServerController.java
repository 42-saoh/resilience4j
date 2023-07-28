package com.example.server1;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping("/app1")
public class ServerController {

    private final CircuitBreaker circuitBreaker1;
    private final CircuitBreaker circuitBreaker2;
    private final Bulkhead bulkhead;
    private final RateLimiter rateLimiter;

    public ServerController(@Name("circuitBreakerConfig") CircuitBreakerConfig circuitBreakerConfig,
                            @Name("circuitBreakerConfig2") CircuitBreakerConfig circuitBreakerConfig2,
                            BulkheadConfig bulkheadConfig, RateLimiterConfig rateLimiterConfig) {
        this.circuitBreaker1 = CircuitBreakerRegistry.of(circuitBreakerConfig).circuitBreaker("myCircuitBreaker");
        this.circuitBreaker2 = CircuitBreaker.of("myCircuitBreaker2", circuitBreakerConfig2);
        this.bulkhead = Bulkhead.of("myBulkhead", bulkheadConfig);
        this.rateLimiter = RateLimiter.of("myRateLimiter", rateLimiterConfig);
    }

    @GetMapping("/a1/{num}")
    public Mono<String> myService(@PathVariable Integer num) {
        WebClient webClient = WebClient.create("http://localhost:8082/bpp1/");

        Mono<String> result = webClient.get()
                .uri(num.toString())
                .retrieve()
                .bodyToMono(String.class);

        // transform(circuitBreaker)은 Mono로 표시되는 반응 스트림에 CircuitBreaker 기능을 적용
        // CircuitBreakerOperator.of(circuitBreaker)는 Resilience4j와 Project Reactor의 통합에 사용
        // Mono 를 보호하기 위해 Mono.transform 메서드와 함께 사용할 수 있는 Transformer를 반환한다.
        // 리액티브 체인은 리액티브 프로그래밍 모델에서 데이터 조각이 통과하는 일련의 작업 또는 단계이다.
        // 데이터 원본으로 시작하여 구독자로 끝난다. 그 사이에 데이터는 일련의 변환, 필터링 및 기타 작업을 거칠 수 있다.
        // 이는 Java 8 이상의 "Stream"과 유사하지만 비동기 데이터와 함께 사용하도록 설계되었다.
        // onErrorResume 는 에러가 발생했을 때 대체할 Mono를 리턴한다.
        // doAfterTerminate 는 Mono가 종료되었을 때 호출된다.
        return result.transform(CircuitBreakerOperator.of(circuitBreaker1))
                .onErrorResume(this::fallbackForMyService)
                .doAfterTerminate(() -> System.out.println("호출 횟수 :" + circuitBreaker1.getMetrics().getNumberOfBufferedCalls()
                        + ", 실패 횟수 : " + circuitBreaker1.getMetrics().getNumberOfFailedCalls()
                        + ", 콜백 호출 횟수 : " + circuitBreaker1.getMetrics().getNumberOfNotPermittedCalls()
                        + ", 성공 횟수 : " + circuitBreaker1.getMetrics().getNumberOfSuccessfulCalls()
                        + ", 느린 호출 횟수 : " + circuitBreaker1.getMetrics().getNumberOfSlowCalls()
                        + ", 느린 호출 율 : " + circuitBreaker1.getMetrics().getSlowCallRate()
                        + ", 실패율 : " + circuitBreaker1.getMetrics().getFailureRate()
                        + ", 상태 : " + circuitBreaker1.getState()
                ));
    }

    @GetMapping("/a2/{num}")
    public Mono<String> myService1(@PathVariable Integer num) {
        WebClient webClient = WebClient.create("http://localhost:8082/bpp1/");

        Mono<String> result = webClient.get()
                .uri(num.toString())
                .retrieve()
                .bodyToMono(String.class);

        return result.transform(CircuitBreakerOperator.of(circuitBreaker2))
                .onErrorResume(this::fallbackForMyService)
                .doAfterTerminate(() -> System.out.println("호출 횟수 :" + circuitBreaker2.getMetrics().getNumberOfBufferedCalls()
                        + ", 실패 횟수 : " + circuitBreaker2.getMetrics().getNumberOfFailedCalls()
                        + ", 콜백 호출 횟수 : " + circuitBreaker2.getMetrics().getNumberOfNotPermittedCalls()
                        + ", 성공 횟수 : " + circuitBreaker2.getMetrics().getNumberOfSuccessfulCalls()
                        + ", 느린 호출 횟수 : " + circuitBreaker2.getMetrics().getNumberOfSlowCalls()
                        + ", 느린 호출 율 : " + circuitBreaker2.getMetrics().getSlowCallRate()
                        + ", 실패율 : " + circuitBreaker2.getMetrics().getFailureRate()
                        + ", 상태 : " + circuitBreaker2.getState()
                ));
    }

    // 이건 계속 초기화된다.
    @GetMapping("/a3/{num}")
    public Mono<String> myService2(@PathVariable Integer num) {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                                                    .failureRateThreshold(25) // 실패율 25% 이상일 경우
                                                    .waitDurationInOpenState(Duration.ofMillis(8000)) // 8초 동안 서킷 오픈 상태 유지
                                                    .slidingWindowSize(10) // 10개의 호출에 대한 실패율을 계산
                                                    .minimumNumberOfCalls(3) // 3개 이상의 호출이 있어야 실패율을 계산
                                                    .slowCallRateThreshold(50) // 느린 호출이 50% 이상이면 서킷 오픈
                                                    .slowCallDurationThreshold(Duration.ofMillis(500)) // 느린 호출의 기준 : 2초 이상 걸릴 경우
                                                    .build();
        CircuitBreaker circuitBreaker3 = CircuitBreakerRegistry.of(circuitBreakerConfig).circuitBreaker("myCircuitBreaker3");
        WebClient webClient = WebClient.create("http://localhost:8082/bpp1/");

        Mono<String> result = webClient.get()
                .uri(num.toString())
                .retrieve()
                .bodyToMono(String.class);

        return result.transform(CircuitBreakerOperator.of(circuitBreaker3))
                .onErrorResume(this::fallbackForMyService)
                .doAfterTerminate(() -> System.out.println("호출 횟수 :" + circuitBreaker3.getMetrics().getNumberOfBufferedCalls()
                        + ", 실패 횟수 : " + circuitBreaker3.getMetrics().getNumberOfFailedCalls()
                        + ", 콜백 호출 횟수 : " + circuitBreaker3.getMetrics().getNumberOfNotPermittedCalls()
                        + ", 성공 횟수 : " + circuitBreaker3.getMetrics().getNumberOfSuccessfulCalls()
                        + ", 느린 호출 횟수 : " + circuitBreaker3.getMetrics().getNumberOfSlowCalls()
                        + ", 느린 호출 율 : " + circuitBreaker3.getMetrics().getSlowCallRate()
                        + ", 실패율 : " + circuitBreaker3.getMetrics().getFailureRate()
                        + ", 상태 : " + circuitBreaker3.getState()
                ));
    }

    @GetMapping("/a4/{num}")
    public Mono<String> myService3(@PathVariable Integer num) {
        WebClient webClient = WebClient.create("http://localhost:8082/bpp1/");
        Mono<String> result = webClient.get()
                .uri(num.toString())
                .retrieve()
                .bodyToMono(String.class);

        // curcuit 은 기존의 것을 사용한다.
        // bulkhead 는 최대 한번에 3개의 호출을 허용, 최대 호출을 넘으면 1000ms 동안 대기
        // tomcat 과 같이 blocking 서버에서는 bulkhead 가 잘 안 될 수 있다. netty 와 같은 non-blocking 서버에서 잘 동작한다고 한다.
        // rateLimiter 는 최대 대기 시간 500ms, 20초에 10개의 호출을 허용(변경)
        return result
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker2))
                .transformDeferred(BulkheadOperator.of(bulkhead))
                .transformDeferred(RateLimiterOperator.of(rateLimiter))
                .onErrorResume(this::fallbackForMyService)
                .doAfterTerminate(() -> {
                    System.out.println("호출 횟수 :" + circuitBreaker2.getMetrics().getNumberOfBufferedCalls()
                        + ", 실패 횟수 : " + circuitBreaker2.getMetrics().getNumberOfFailedCalls()
                        + ", 콜백 호출 횟수 : " + circuitBreaker2.getMetrics().getNumberOfNotPermittedCalls()
                        + ", 성공 횟수 : " + circuitBreaker2.getMetrics().getNumberOfSuccessfulCalls()
                        + ", 느린 호출 횟수 : " + circuitBreaker2.getMetrics().getNumberOfSlowCalls()
                        + ", 느린 호출 율 : " + circuitBreaker2.getMetrics().getSlowCallRate()
                        + ", 실패율 : " + circuitBreaker2.getMetrics().getFailureRate()
                        + ", 상태 : " + circuitBreaker2.getState());
                    System.out.println("가능 횟수 :" + bulkhead.getMetrics().getAvailableConcurrentCalls()
                            + ", 최대 횟수 : " + bulkhead.getMetrics().getMaxAllowedConcurrentCalls());
                    System.out.println("가능 횟수 :" + rateLimiter.getMetrics().getAvailablePermissions()
                        + ", 대기하는 쓰레드 : " + rateLimiter.getMetrics().getNumberOfWaitingThreads());
                });
    }

    public Mono<String> fallbackForMyService(Throwable e) {
        return Mono.just("기본광고를 시작합니다.");
    }
}
