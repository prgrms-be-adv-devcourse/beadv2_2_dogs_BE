package com.barofarm.support.review.client.order;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderFeignConfig {
    @Bean
    public ErrorDecoder orderFeignErrorDecoder() {
        return new OrderFeignErrorDecoder();
    }
}
