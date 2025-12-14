package com.barofarm.buyer.product.presentation.internal;

import com.barofarm.buyer.product.application.ProductInternalService;
import com.barofarm.buyer.product.application.dto.internal.ReviewProductInfo;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/products")
@RequiredArgsConstructor
public class ProductInternalController {

    private final ProductInternalService productInternalService;

    @GetMapping("/{id}")
    public ReviewProductInfo getInternalProductDetail(@PathVariable UUID id) {
        return productInternalService.getInternalProductDetail(id);
    }
}
