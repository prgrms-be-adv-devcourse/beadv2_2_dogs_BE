package com.barofarm.ai.season.infrastructure.client;

import com.barofarm.ai.season.infrastructure.client.dto.ProductSummary;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Buyer 서비스의 상품 조회를 위한 Feign Client
 */
@FeignClient(name = "buyer-service")
public interface ProductQueryFeignClient {

    /**
     * 제철 정보가 없는 상품 목록 조회
     *
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @return 제철 정보가 없는 상품 목록
     */
    @GetMapping("/internal/products/without-seasonality")
    List<ProductSummary> getProductsWithoutSeasonality(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );
}
