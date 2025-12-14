package com.barofarm.buyer.product.application;

import com.barofarm.buyer.common.exception.CustomException;
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

  public ProductDetailInfo createProduct(ProductCreateCommand command) {
    log.info("🚀 [PRODUCT_SERVICE] Starting product creation - Seller ID: {}, Product Name: {}, Category: {}, Price: {}, Stock: {}", 
        command.sellerId(), command.productName(), command.productCategory(), command.price(), command.stockQuantity());
    
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
    
    log.info("📦 [PRODUCT_SERVICE] Product entity created - ID: {}, Name: {}", 
        product.getId(), product.getProductName());

    productRepository.save(product);
    log.info("💾 [PRODUCT_SERVICE] Product saved to database - ID: {}, Name: {}, Seller: {}", 
        product.getId(), product.getProductName(), product.getSellerId());

    // 카프카 이벤트 발행
    log.info("📤 [PRODUCT_SERVICE] Publishing PRODUCT_CREATED event to Kafka - Product ID: {}, Name: {}", 
        product.getId(), product.getProductName());
    productEventPublisher.publishProductCreated(product);

    ProductDetailInfo result = ProductDetailInfo.from(product);
    log.info("✅ [PRODUCT_SERVICE] Product creation completed - ID: {}, Name: {}", 
        result.id(), result.productName());
    return result;
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

      // 카프카 이벤트 발행
      productEventPublisher.publishProductUpdated(product);

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
