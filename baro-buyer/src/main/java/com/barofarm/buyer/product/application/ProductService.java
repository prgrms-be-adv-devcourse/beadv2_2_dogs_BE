package com.barofarm.buyer.product.application;

import com.barofarm.buyer.common.exception.CustomException;
import com.barofarm.buyer.common.response.CustomPage;
import com.barofarm.buyer.product.application.dto.ProductCreateCommand;
import com.barofarm.buyer.product.application.dto.ProductDetailInfo;
import com.barofarm.buyer.product.application.dto.ProductUpdateCommand;
import com.barofarm.buyer.product.application.event.ProductEventPublisher;
import com.barofarm.buyer.product.domain.Product;
import com.barofarm.buyer.product.domain.ProductRepository;
import com.barofarm.buyer.product.domain.ProductStatus;
import com.barofarm.buyer.product.exception.ProductErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

  private final ProductRepository productRepository;
  private final ProductEventPublisher productEventPublisher;

  @Transactional(readOnly = true)
  public ProductDetailInfo getProductDetail(UUID id) {
    Product product =
        productRepository
            .findById(id)
            .orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));

    return ProductDetailInfo.from(product);
  }

    @Transactional(readOnly = true)
    public CustomPage<ProductDetailInfo> getProducts(Pageable pageable) {
        Page<ProductDetailInfo> products = productRepository.findAll(pageable)
            .map(ProductDetailInfo::from);

        return CustomPage.from(products);
    }

  public ProductDetailInfo createProduct(ProductCreateCommand command) {
    //      MemberRole memberRole = MemberRole.from(role);
    //
    //      if (memberRole != MemberRole.SELLER) {
    //          throw new CustomException(ErrorCode.FORBIDDEN_ONLY_SELLER);
    //      }

    Product product =
        Product.create(
            command.sellerId(),
            command.productName(),
            command.description(),
            command.productCategory(),
            command.price(),
            command.stockQuantity(),
            ProductStatus.ON_SALE);

      if (command.imageUrls() != null) {
          product.replaceImages(command.imageUrls());
      }

      Product savedProduct = productRepository.save(product);

      // 카프카 이벤트 발행
      log.info("📤 [PRODUCT_SERVICE] Publishing PRODUCT_CREATED event to Kafka - Product ID: {}, Name: {}",
        product.getId(), product.getProductName());
      productEventPublisher.publishProductCreated(product);

      return ProductDetailInfo.from(savedProduct);
  }

  public ProductDetailInfo updateProduct(UUID id, ProductUpdateCommand command) {
    Product product =
        productRepository
            .findById(id)
            .orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));

    //    MemberRole memberRole = MemberRole.from(role);
    //
    //    if (memberRole != MemberRole.SELLER) {
    //      throw new CustomException(ErrorCode.FORBIDDEN_ONLY_SELLER);
    //    }

    product.validateOwner(command.memberId());

    product.update(
        command.productName(),
        command.description(),
        command.productCategory(),
        command.price(),
        command.stockQuantity(),
        command.productStatus());

    return ProductDetailInfo.from(product);
  }

    public void deleteProduct(UUID id, UUID memberId, String role) {
    Product product =
        productRepository
            .findById(id)
            .orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));

    //    MemberRole memberRole = MemberRole.from(role);
    //
    //    if (memberRole != MemberRole.SELLER) {
    //      throw new CustomException(ErrorCode.FORBIDDEN_ONLY_SELLER);
    //    }

    product.validateOwner(memberId);

    productRepository.deleteById(id);

      // 카프카 이벤트 발행
      productEventPublisher.publishProductDeleted(product);
  }
}
