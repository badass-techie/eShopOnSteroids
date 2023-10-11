package com.badasstechie.apigateway.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.WebFilter;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;

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
                        .pathMatchers("/api/v*/identity/**").permitAll()    // so that new users can register
                        .pathMatchers(HttpMethod.GET, "/api/v*/product/**").permitAll()     // so that new users can see the products
                        .pathMatchers("/api/v*/*/swagger-ui.html").permitAll()     // so that the docs can be accessed
                        .pathMatchers("/api/v*/*/swagger-ui/**").permitAll()
                        .pathMatchers("/api/v*/*/swagger-resources/**").permitAll()
                        .pathMatchers("/api/v*/*/api-docs/**").permitAll()
                        .pathMatchers("/actuator/**").permitAll()   // so that actuator endpoints for the api gateway can be accessed
                        .pathMatchers("/eureka/*").permitAll() // admin services like eureka will implement their own auth
                        .anyExchange().authenticated()  // require a a valid token for all other requests
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

            try {
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
            } catch (JwtValidationException jwtValidationException) {
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);    // set the status code to 401
                DataBuffer buffer = response.bufferFactory().wrap(jwtValidationException.getMessage().getBytes(StandardCharsets.UTF_8));
                return response.writeWith(Mono.just(buffer)); // write the exception message to the response
            }
        };
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        return NimbusReactiveJwtDecoder.withPublicKey(rsaKeyProperties.publicKey()).build();
    }
}
