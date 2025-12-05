package com.barofarm.auth;

import java.time.Clock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class AuthServiceApplication {

  public static void main(String[] args) {

    SpringApplication.run(AuthServiceApplication.class, args);
  }

  // Clock 설정
  @Bean
  public Clock clock() {
    return Clock.systemDefaultZone();
  }
}
