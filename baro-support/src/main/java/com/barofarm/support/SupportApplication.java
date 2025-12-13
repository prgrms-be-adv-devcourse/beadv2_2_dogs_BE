package com.barofarm.support;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
// import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
// import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableKafka
// @EnableDiscoveryClient
// @EnableFeignClients
public class SupportApplication {

    public static void main(String[] args) {
        SpringApplication.run(SupportApplication.class, args);
    }
}
