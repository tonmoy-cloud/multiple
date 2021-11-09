package com.infoworks.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

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
                //Kick-out from here:
                return unauthorizedAccessHandler(exchange, HttpStatus.UNAUTHORIZED);
            }

            String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            if (authHeader == null
                    || authHeader.isEmpty() || !authHeader.startsWith("Bearer")){
                //Kick-out from here:
                return unauthorizedAccessHandler(exchange, HttpStatus.UNAUTHORIZED);
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
                            return unauthorizedAccessHandler(exchange, clientResponse.statusCode());
                        } else {
                            //Passing down the stream:
                            return chain.filter(exchange);
                        }
                    });
            //
            return filterChain;
        };
    }

    private static Mono<Void> unauthorizedAccessHandler(ServerWebExchange exchange, HttpStatus status){
        return unauthorizedAccessHandler(exchange, status, "Un-Authorized Access!");
    }

    private static Mono<Void> unauthorizedAccessHandler(ServerWebExchange exchange, HttpStatus status, Object body) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setLocation(URI.create("/error/unauthorized.html"));
        //
        if (body != null){
            DataBufferFactory dataBufferFactory = exchange.getResponse().bufferFactory();
            ObjectMapper objMapper = new ObjectMapper();
            try {
                byte[] obj = objMapper.writeValueAsBytes(body);
                return response.writeWith(Mono.just(obj).map(r -> dataBufferFactory.wrap(r)));
            } catch (Exception e) {}
        }
        //
        return response.setComplete();
    }

}
