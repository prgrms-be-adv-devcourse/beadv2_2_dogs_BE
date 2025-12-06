package com.barofarm.buyer.product.application;

import com.barofarm.buyer.common.exception.CustomException;
import com.barofarm.buyer.common.exception.ErrorCode;
import com.barofarm.buyer.product.application.dto.ProductCreateCommand;
import com.barofarm.buyer.product.application.dto.ProductDetailInfo;
import com.barofarm.buyer.product.application.dto.ProductUpdateCommand;
import com.barofarm.buyer.product.domain.Product;
import com.barofarm.buyer.product.domain.ProductRepository;
import com.barofarm.buyer.product.domain.ProductStatus;
import java.util.UUID;

public class ProductService {

  private final ProductRepository productRepository;

  public ProductService(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  public ProductDetailInfo getProductDetail(UUID id) {
    Product product =
        productRepository
            .findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

    return ProductDetailInfo.from(product);
  }

  public ProductDetailInfo createProduct(ProductCreateCommand command) {
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
            .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

    product.update(
        command.productName(),
        command.description(),
        command.productCategory(),
        command.price(),
        command.stockQuantity(),
        command.productStatus());

    return ProductDetailInfo.from(product);
  }

  public void deleteProduct(UUID id, UUID memberId) {
    Product product =
        productRepository
            .findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

    product.validateOwner(memberId);

    productRepository.deleteById(id);
  }
}
