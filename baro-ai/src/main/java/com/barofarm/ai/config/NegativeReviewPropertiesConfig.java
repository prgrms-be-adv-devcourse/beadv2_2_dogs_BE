package com.barofarm.ai.config;

import com.barofarm.ai.review.infrastructure.negative_review.NegativeReviewProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(NegativeReviewProperties.class)
public class NegativeReviewPropertiesConfig {
}
