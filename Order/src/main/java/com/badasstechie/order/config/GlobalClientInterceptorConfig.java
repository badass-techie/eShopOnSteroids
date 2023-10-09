package com.badasstechie.order.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.grpc.ClientInterceptor;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class GlobalClientInterceptorConfig {
    protected final CircuitBreakerRegistry circuitBreakerRegistry;

    @GrpcGlobalClientInterceptor
    ClientInterceptor circuitBreakerClientInterceptor() {
        return new CircuitBreakerClientInterceptor(circuitBreakerRegistry.circuitBreaker("product-grpc-service"));
    }
}
