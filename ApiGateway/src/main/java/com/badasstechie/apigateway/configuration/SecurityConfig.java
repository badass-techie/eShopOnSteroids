package com.badasstechie.apigateway.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.WebFilter;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final RsaKeyProperties rsaKeyProperties;

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http, WebFilter authFilter){
        return http
                .addFilterAt(authFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/api/v*/identity/**").permitAll()    // identity microservice will be accessible to everyone to request or manage credentials
                        .pathMatchers("/eureka/**").permitAll() // admin services like eureka will implement their own auth
                        .anyExchange().authenticated()  // requests to the other microservices will require a valid token
                )
                .oauth2ResourceServer((oauth2) -> oauth2
                        .jwt(Customizer.withDefaults())
                )
                .build();
    }

    @Bean
    public WebFilter authFilter(ReactiveJwtDecoder jwtDecoder) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String authorizationHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);
                Jwt jwt = jwtDecoder.decode(token).share().block();
                String userId = jwt.getSubject();   // get the subject which is the user id

                // add the user id as a query param to the request
                URI uri = UriComponentsBuilder.fromUri(request.getURI())
                        .queryParam("userId", userId)
                        .build()
                        .toUri();
                request = request.mutate().uri(uri).build();
            }

            return chain.filter(exchange.mutate().request(request).build());
        };
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        return NimbusReactiveJwtDecoder.withPublicKey(rsaKeyProperties.publicKey()).build();
    }
}
