package com.barofarm.gateway.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public GroupedOpenApi gatewayApi() {
    return GroupedOpenApi.builder().group("gateway").pathsToMatch("/**").build();
  }
}
