package com.barofarm.buyer.product.application;

import com.barofarm.buyer.product.application.dto.internal.ProductSummary;
import com.barofarm.buyer.product.application.dto.internal.ReviewProductInfo;
import com.barofarm.buyer.product.domain.Product;
import com.barofarm.buyer.product.domain.ProductRepository;
import com.barofarm.buyer.product.domain.SeasonalityType;
import com.barofarm.buyer.product.exception.ProductErrorCode;
import com.barofarm.exception.CustomException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductInternalService {

    private final ProductRepository productRepository;

    public ReviewProductInfo getInternalProductDetail(UUID id) {
        Product product =
            productRepository
                .findById(id)
                .orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));

        return ReviewProductInfo.from(product);
    }

    public void updateSeasonality(UUID productId, SeasonalityType seasonalityType, String seasonalityValue) {
        Product product =
            productRepository
                .findById(productId)
                .orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));

        product.updateSeasonality(seasonalityType, seasonalityValue);
        productRepository.save(product);
    }

    /**
     * 제철 정보가 없는 상품 목록 조회
     *
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 제철 정보가 없는 상품 목록
     */
    @Transactional(readOnly = true)
    public List<ProductSummary> getProductsWithoutSeasonality(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findAllWithoutSeasonality(pageable);
        return products.getContent().stream()
            .map(ProductSummary::from)
            .toList();
    }
}
