package com.infoworks.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@PropertySource("classpath:service-names.properties")
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    private WebClient.Builder builder;

    public AuthFilter(@Qualifier("LoadBalancedClientBuilder") WebClient.Builder builder) {
        super(Config.class);
        this.builder = builder;
    }

    @Value("${app.auth.validation.url}")
    private String authValidationURL;

    @Override
    public GatewayFilter apply(Config config) {
        return createGatewayFilter(builder, authValidationURL);
    }

    public static class Config {}

    public static GatewayFilter createGatewayFilter(WebClient.Builder builder, String authValidationURL){
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
                    //.get()
                    //.uri(String.format("%s?token=%s", authValidationURL, token))
                    //OR following
                    .post()
                    .uri(authValidationURL)
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .exchange()
                    .flatMap(clientResponse -> {
                        if (clientResponse.statusCode() == HttpStatus.SERVICE_UNAVAILABLE
                            || clientResponse.statusCode() == HttpStatus.NOT_FOUND
                            || clientResponse.statusCode() == HttpStatus.BAD_REQUEST
                            || clientResponse.statusCode() == HttpStatus.INTERNAL_SERVER_ERROR
                            || clientResponse.statusCode() == HttpStatus.UNAUTHORIZED){
                            //Kick-out from here:
                            ServerHttpResponse response = exchange.getResponse();
                            response.setStatusCode(clientResponse.statusCode());
                            return response.setComplete();
                        } else {
                            //Passing down the stream:
                            return chain.filter(exchange);
                        }
                    });
            //
            return filterChain;
        };
    }

}
