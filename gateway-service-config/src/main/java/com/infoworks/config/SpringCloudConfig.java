package com.infoworks.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
@PropertySource("classpath:service-names.properties")
public class SpringCloudConfig {

    @Value("${app.first.url}")
    private String firstURL;

    @Value("${app.second.url}")
    private String secondURL;

    @Value("${app.auth.url}")
    private String authURL;

    @Value("${app.auth.validation.url}")
    private String authValidationURL;

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
        return AuthFilter.createGatewayFilter(builder, authValidationURL);
    }

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder
                        , @Qualifier("CustomAuthFilter") GatewayFilter authFilter) {
        return builder.routes()
                .route("employeeModule"
                        , r -> r.path("/employee/**")
                            .uri(firstURL)
                            .filter(authFilter))
                .route("consumerModule"
                        , r -> r.path("/consumer/**")
                            .uri(secondURL))
                /*.route("authModule"
                        , r -> r.path("/auth/**")
                            .uri(authURL))*/
                .build();
    }

}