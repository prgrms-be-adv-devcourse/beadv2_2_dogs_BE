package com.barofarm.support.settlement.client;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderItemFeignConfig {
    @Bean
    public ErrorDecoder orderItemFeignErrorDecoder() {
        return new OrderItemFeignErrorDecoder();
    }
}
