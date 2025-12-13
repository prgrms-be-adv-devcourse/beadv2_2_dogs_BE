package com.barofarm.buyer.product.infrastructure;

import com.barofarm.buyer.product.domain.Product;
import com.barofarm.buyer.product.domain.ProductRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepositoryAdapter implements ProductRepository {

  private final ProductJpaRepository productJpaRepository;

  public ProductRepositoryAdapter(ProductJpaRepository productJpaRepository) {
    this.productJpaRepository = productJpaRepository;
  }

  @Override
  public Page<Product> findAll(Pageable pageable) {
    return productJpaRepository.findAll(pageable);
  }

  @Override
  public Optional<Product> findById(UUID id) {
    return productJpaRepository.findById(id);
  }

  @Override
  public Product save(Product product) {
    return productJpaRepository.save(product);
  }

  @Override
  public void deleteById(UUID id) {
    productJpaRepository.deleteById(id);
  }
}
