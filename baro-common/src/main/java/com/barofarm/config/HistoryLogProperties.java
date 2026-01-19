package com.barofarm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "history.log")
@Getter
@Setter
public class HistoryLogProperties {
    private boolean enabled = true;
    private String aiTopic = "ai-history";
    private String cartTopic = "cart-events";
    private String orderTopic = "order-events";
    private String userIdHeader = "X-User-Id";
}
