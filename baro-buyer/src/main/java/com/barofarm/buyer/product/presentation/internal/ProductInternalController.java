package com.barofarm.buyer.product.presentation.internal;

import com.barofarm.buyer.product.application.ProductInternalService;
import com.barofarm.buyer.product.application.dto.internal.ProductSummary;
import com.barofarm.buyer.product.application.dto.internal.ReviewProductInfo;
import com.barofarm.buyer.product.presentation.dto.SeasonalityUpdateRequest;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @PutMapping("/{id}/seasonality")
    public void updateSeasonality(
            @PathVariable UUID id,
            @RequestBody SeasonalityUpdateRequest request) {
        productInternalService.updateSeasonality(id, request.seasonalityType(), request.seasonalityValue());
    }

    @GetMapping("/without-seasonality")
    public List<ProductSummary> getProductsWithoutSeasonality(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return productInternalService.getProductsWithoutSeasonality(page, size);
    }
}
