package com.barofarm.support.review.client;

import com.barofarm.support.review.client.dto.ProductResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "product-service",
    configuration = FeignConfig.class
)
public interface ProductClient {

    @GetMapping("/internal/products/{id}")
    ProductResponse getProduct(@PathVariable("id") UUID productId);
}
