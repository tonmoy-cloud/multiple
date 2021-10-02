package com.infoworks.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class BeanConfig {

    @Bean("LoadBalancedClientBuilder") @LoadBalanced
    public WebClient.Builder loadBalancedWebclientBuilder(){
        return WebClient.builder();
    }

}
