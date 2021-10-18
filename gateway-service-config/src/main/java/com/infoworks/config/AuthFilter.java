package com.infoworks.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
            Mono<Void> filterChain = builder.build()
                    .get()
                    .uri("http://localhost:8083/auth/validateToken?token=" + token)
                    //OR following
                    //.post()
                    //.uri("http://localhost:8083/auth/validateToken")
                    //.header(HttpHeaders.AUTHORIZATION, authHeader)
                    .exchange()
                    .map(clientResponse -> {
                        if (clientResponse.statusCode() == HttpStatus.SERVICE_UNAVAILABLE
                            || clientResponse.statusCode() == HttpStatus.NOT_FOUND
                            || clientResponse.statusCode() == HttpStatus.BAD_REQUEST
                            || clientResponse.statusCode() == HttpStatus.INTERNAL_SERVER_ERROR
                            || clientResponse.statusCode() == HttpStatus.UNAUTHORIZED){
                            exchange.getResponse()
                                    .setStatusCode(clientResponse.statusCode());
                        }
                        return exchange;
                    })
                    .flatMap(webExchange -> chain.filter(webExchange));
            //
            return filterChain;
        };
    }

}