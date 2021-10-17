package com.infoworks.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class SpringCloudConfig {

    @Bean
    public GlobalFilter globalFilter() {
        return (exchange, chain) -> {
            System.out.println("Pre Global filter");
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                //
                System.out.println("Post Global filter");
            }));
        };
    }

    @Bean("CustomAuthFilter")
    public GatewayFilter getAuthFilter(@Qualifier("LoadBalancedClientBuilder") WebClient.Builder builder){
        return AuthFilter.createGatewayFilter(builder);
    }

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder
                        , @Qualifier("CustomAuthFilter") GatewayFilter authFilter) {
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