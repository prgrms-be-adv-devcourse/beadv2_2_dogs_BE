package com.barofarm.support.common.client;

import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Seller Service의 Farm API를 호출하는 Feign 클라이언트
 */
@FeignClient(name = "seller-service", path = "/api/v1/farms")
public interface FarmClient {

    /**
     * 사용자 ID로 해당 사용자가 소유한 농장 ID 조회
     *
     * @param userId 사용자 ID
     * @return 농장 ID
     */
    @GetMapping("/users/{userId}/farm-id")
    UUID getFarmIdByUserId(@PathVariable("userId") UUID userId);
}
