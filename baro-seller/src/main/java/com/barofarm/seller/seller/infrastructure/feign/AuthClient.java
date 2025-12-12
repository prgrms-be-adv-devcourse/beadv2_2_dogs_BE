package com.barofarm.seller.seller.infrastructure.feign;

import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
    name = "auth-service",
    path = "/auth",
    configuration = com.barofarm.seller.seller.config.FeignAuthConfig.class
)
public interface AuthClient {

    @PostMapping("/{userId}/grant-seller")
    void grantSeller(@PathVariable("userId") UUID userId);
}
