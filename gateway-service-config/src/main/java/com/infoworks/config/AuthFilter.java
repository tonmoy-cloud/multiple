package com.infoworks.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    private WebClient.Builder builder;

    public AuthFilter(@Qualifier("LoadBalancedClientBuilder") WebClient.Builder builder) {
        super(Config.class);
        this.builder = builder;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return createGatewayFilter(builder);
    }

    public static class Config {}

    public static GatewayFilter createGatewayFilter(WebClient.Builder builder){
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

            //Make authentication call to Validate-Token-API:
            String token = authHeader.substring("Bearer ".length());
            ClientResponse response = builder.build()
                    .post()
                    .uri("https://auth-service/validateToken?token=" + token)
                    //OR following
                    //.uri("https://auth-service/validateToken")
                    //.header(HttpHeaders.AUTHORIZATION, authHeader)
                    .exchange()
                    .block(Duration.ofMillis(1000));
            HttpStatus statusCode = response.statusCode();
            response.bodyToMono(Void.class);
            //
            if (statusCode == HttpStatus.UNAUTHORIZED){
                throw new RuntimeException("Un-Authorized Access!");
            }
            //
            return chain.filter(exchange);
        };
    }

}
