package com.barofarm.buyer.product.application;

import com.barofarm.buyer.inventory.application.InventoryService;
import com.barofarm.buyer.product.application.dto.ProductCreateCommand;
import com.barofarm.buyer.product.application.dto.ProductDetailInfo;
import com.barofarm.buyer.product.application.dto.ProductUpdateCommand;
import com.barofarm.buyer.product.application.event.ProductTransactionEvent;
import com.barofarm.buyer.product.domain.Category;
import com.barofarm.buyer.product.domain.CategoryRepository;
import com.barofarm.buyer.product.domain.Product;
import com.barofarm.buyer.product.domain.ProductRepository;
import com.barofarm.buyer.product.domain.ProductStatus;
import com.barofarm.buyer.product.domain.UserType;
import com.barofarm.buyer.product.exception.ProductErrorCode;
import com.barofarm.dto.CustomPage;
import com.barofarm.exception.CustomException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryService inventoryService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional(readOnly = true)
    public ProductDetailInfo getProductDetail(UUID id) {
        Product product =
            productRepository.findById(id)
            .orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));

        // TO-DO
        // int stock = inventoryService.getInventory(id);
        int stock = 0;

        return ProductDetailInfo.from(product, stock);
    }

    @Transactional(readOnly = true)
    public CustomPage<ProductDetailInfo> getProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);

        List<UUID> productIds = products.getContent().stream()
            .map(Product::getId)
            .toList();

//        TO-DO 재고 찾기
//        Map<UUID, Integer> stockMap = inventoryService.getStocksByProductIds(productIds);
//
//        Page<ProductDetailInfo> infos = products.map(p ->
//            ProductDetailInfo.from(p, stockMap.getOrDefault(p.getId(), 0))
//        );
//
//        return CustomPage.from(infos);
        return null;
    }

    public ProductDetailInfo createProduct(ProductCreateCommand command) {
        // user의 역할이 isSeller가 아니라면 에러 호출
        validateSeller(command.role());

        Category category = getCategory(command.categoryId());
        Product product = Product.create(
            command.sellerId(),
            command.productName(),
            command.description(),
            category,
            command.price(),
            ProductStatus.ON_SALE);

        // To-do 이미지 저장

        // 상품 저장 및 재고 저장
        Product savedProduct = saveProductAndInventory(product, command.stockQuantity());

        return ProductDetailInfo.from(savedProduct, command.stockQuantity());
    }

    @Transactional
    public Product saveProductAndInventory(Product product, Integer stockQuantity) {
        Product savedProduct = productRepository.save(product);

        //재고 생성 로직
        //inventoryService.create(UUID productId, command.stockQuantity);

        // 트랜잭션 이벤트 발행 (트랜잭션 성공 시에만 카프카 이벤트 발행됨)
        ProductTransactionEvent event = new ProductTransactionEvent(savedProduct,
                ProductTransactionEvent.ProductOperation.CREATED);
        applicationEventPublisher.publishEvent(event);

        return savedProduct;
    }

    public ProductDetailInfo updateProduct(UUID id, ProductUpdateCommand command) {
        // user의 역할이 isSeller가 아니라면 에러 호출
        validateSeller(command.role());

        Product product =
            productRepository.findById(id)
                .orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));

        product.validateOwner(command.memberId());

        Category category = getCategory(command.categoryId());
        product.update(
            command.productName(),
            command.description(),
            category,
            command.price(),
            command.productStatus());

        // TO-DO 이미지 업데이트

        Product savedProduct = updateProductAndInventory(product, command.stockQuantity());

        return ProductDetailInfo.from(savedProduct, command.stockQuantity());
    }

    @Transactional
    public Product updateProductAndInventory(Product product, Integer stockQuantity) {
        Product updatedProduct = productRepository.save(product);

        //재고 업데이트 로직
        //inventoryService.update(UUID productId, command.stockQuantity);

        // 트랜잭션 이벤트 발행 (트랜잭션 성공 시에만 카프카 이벤트 발행됨)
        ProductTransactionEvent event = new ProductTransactionEvent(updatedProduct,
            ProductTransactionEvent.ProductOperation.UPDATED);
        applicationEventPublisher.publishEvent(event);

        return updatedProduct;
    }

    @Transactional
    public void deleteProduct(UUID id, UUID memberId, UserType userType) {
        // user의 역할이 isSeller가 아니라면 에러 호출
        validateSeller(userType);

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));

        product.validateOwner(memberId);

        productRepository.deleteById(id);

        // 트랜잭션 이벤트 발행 (트랜잭션 성공 시에만 카프카 이벤트 발행됨)
        ProductTransactionEvent event = new ProductTransactionEvent(product,
            ProductTransactionEvent.ProductOperation.DELETED);
        applicationEventPublisher.publishEvent(event);
    }

    private static void validateSeller(UserType userType) {
        if(!userType.isSeller()){
            throw new CustomException(ProductErrorCode.FORBIDDEN_ONLY_SELLER);
        }
    }

    private Category getCategory(UUID categoryId) {
        return categoryRepository.findById(categoryId)
            .orElseThrow(() -> new CustomException(ProductErrorCode.CATEGORY_NOT_FOUND));
    }
}
