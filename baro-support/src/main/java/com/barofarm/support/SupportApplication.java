package com.barofarm.support;

import com.barofarm.config.JpaAuditingConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableKafka
@EnableAsync
@EnableFeignClients(basePackages = "com.barofarm.support")
@Import(JpaAuditingConfig.class)
public class SupportApplication {

    public static void main(String[] args) {
        SpringApplication.run(SupportApplication.class, args);
    }
}
