package com.barofarm.auth.infrastructure.oauth;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({NaverOAuthProperties.class, KakaoOAuthProperties.class})
public class OAuthConfig {
}
