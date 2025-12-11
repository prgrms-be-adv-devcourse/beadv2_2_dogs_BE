package com.barofarm.order.order.config;

import com.barofarm.order.order.client.InventoryErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class InventoryFeignConfig {

    @Bean
    public ErrorDecoder inventoryErrorDecoder() {
        return new InventoryErrorDecoder();
    }
}
