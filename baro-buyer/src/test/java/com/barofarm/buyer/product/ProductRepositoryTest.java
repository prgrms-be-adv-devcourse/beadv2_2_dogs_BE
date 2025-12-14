package com.barofarm.buyer.product;

import static org.assertj.core.api.Assertions.assertThat;

import com.barofarm.buyer.config.BaseRepositoryTest;
import com.barofarm.buyer.product.domain.Product;
import com.barofarm.buyer.product.domain.ProductCategory;
import com.barofarm.buyer.product.domain.ProductStatus;
import com.barofarm.buyer.product.infrastructure.ProductRepositoryAdapter;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

@Import(ProductRepositoryAdapter.class)
public class ProductRepositoryTest extends BaseRepositoryTest {

    @Nested
    @DisplayName("save 메서드 테스트")
    class Save {
        @Test
        @DisplayName("제품 정보를 저장한다.")
        void success() {
            //given
            UUID sellerId = UUID.randomUUID();
            Product product = Product.create(
                sellerId,
                "감자",
                "직접 재배한 감자입니다",
                ProductCategory.ROOT,
                15000L,
                1,
                ProductStatus.ON_SALE
            );

            //when
            productRepository.save(product);
            Product savedProduct = productRepository.findById(product.getId()).get();

            //then
            assertThat(savedProduct.getId()).isEqualTo(product.getId());
            assertThat(savedProduct.getSellerId()).isEqualTo(product.getSellerId());
            assertThat(savedProduct.getProductName()).isEqualTo(product.getProductName());
            assertThat(savedProduct.getDescription()).isEqualTo(product.getDescription());
            assertThat(savedProduct.getProductCategory()).isEqualTo(product.getProductCategory());
            assertThat(savedProduct.getPrice()).isEqualTo(product.getPrice());
            assertThat(savedProduct.getStockQuantity()).isEqualTo(product.getStockQuantity());
            assertThat(savedProduct.getProductStatus()).isEqualTo(product.getProductStatus());
        }
    }
}
