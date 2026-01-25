package com.barofarm.ai.config;

import com.barofarm.ai.review.infrastructure.sentiment.SentimentProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SentimentProperties.class)
public class SentimentPropertiesConfig {
}
