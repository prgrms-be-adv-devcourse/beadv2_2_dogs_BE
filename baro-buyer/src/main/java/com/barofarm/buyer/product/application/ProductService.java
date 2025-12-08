package com.barofarm.buyer.product.application;

import com.barofarm.buyer.common.exception.CustomException;
import com.barofarm.buyer.product.application.dto.ProductCreateCommand;
import com.barofarm.buyer.product.application.dto.ProductDetailInfo;
import com.barofarm.buyer.product.application.dto.ProductUpdateCommand;
import com.barofarm.buyer.product.domain.Product;
import com.barofarm.buyer.product.domain.ProductRepository;
import com.barofarm.buyer.product.domain.ProductStatus;
import com.barofarm.buyer.product.exception.FarmErrorCode;
import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

  private final ProductRepository productRepository;

  public ProductDetailInfo getProductDetail(UUID id) {
    Product product =
        productRepository
            .findById(id)
            .orElseThrow(() -> new CustomException(FarmErrorCode.PRODUCT_NOT_FOUND));

    return ProductDetailInfo.from(product);
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

    productRepository.save(product);

    return ProductDetailInfo.from(product);
  }

  public ProductDetailInfo updateProduct(UUID id, ProductUpdateCommand command) {
    Product product =
        productRepository
            .findById(id)
            .orElseThrow(() -> new CustomException(FarmErrorCode.PRODUCT_NOT_FOUND));

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
            .orElseThrow(() -> new CustomException(FarmErrorCode.PRODUCT_NOT_FOUND));

    //    MemberRole memberRole = MemberRole.from(role);
    //
    //    if (memberRole != MemberRole.SELLER) {
    //      throw new CustomException(ErrorCode.FORBIDDEN_ONLY_SELLER);
    //    }

    product.validateOwner(memberId);

    productRepository.deleteById(id);
  }
}
