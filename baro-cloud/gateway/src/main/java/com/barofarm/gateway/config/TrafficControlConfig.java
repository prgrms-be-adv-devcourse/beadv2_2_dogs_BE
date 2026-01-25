package com.barofarm.gateway.config;

import java.net.InetSocketAddress;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class TrafficControlConfig {

    @Bean
    public KeyResolver gatewayKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId != null && !userId.isBlank()) {
                return Mono.just("user:" + userId);
            }
            InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
            String host = remoteAddress == null || remoteAddress.getAddress() == null
                ? "unknown"
                : remoteAddress.getAddress().getHostAddress();
            return Mono.just("ip:" + host);
        };
    }
}
