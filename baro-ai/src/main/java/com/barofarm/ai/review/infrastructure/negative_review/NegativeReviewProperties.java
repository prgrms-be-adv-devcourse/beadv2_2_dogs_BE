package com.barofarm.ai.review.infrastructure.negative_review;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "review")
public class NegativeReviewProperties {
    private Map<String, List<String>> categories = new HashMap<>();
}
