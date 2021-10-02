package com.infoworks.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class SpringCloudConfig {

    @Bean
    public GlobalFilter globalFilter() {
        return (exchange, chain) -> {
            System.out.println("Pre Global filter");
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                System.out.println("Post Global filter");
            }));
        };
    }

    @Bean("AuthFilter")
    public GatewayFilter getAuthFilter(@Qualifier("LoadBalancedClientBuilder") WebClient.Builder builder){
        return (exchange, chain) -> {
            //Checking Authorization Header Attribute:
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)){
                throw new RuntimeException("Un-Authorized Access!");
            }

            String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            if (authHeader == null
                    || authHeader.isEmpty() || !authHeader.startsWith("Bearer")){
                throw new RuntimeException("Un-Authorized Access!");
            }

            //TODO:
            String token = authHeader.substring("Bearer ".length());
            builder.build()
                    .post().uri("http://auth-service/validateToken?token" + token)
                    .exchange().block();

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                System.out.println("Post Auth filter");
            }));
        };
    }

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder
                        , @Qualifier("AuthFilter") GatewayFilter authFilter) {
        return builder.routes()
                .route(r -> r.path("/employee/**")
                        .uri("http://localhost:8081/")
                        .filter(authFilter)
                        .id("employeeModule"))

                .route(r -> r.path("/consumer/**")
                        .uri("http://localhost:8082/")
                        .id("consumerModule"))
                .build();
    }

}