package com.barofarm.buyer.product.application;

import com.barofarm.buyer.inventory.application.InventoryService;
import com.barofarm.buyer.inventory.application.dto.request.InventoryCreateCommand;
import com.barofarm.buyer.inventory.domain.Inventory;
import com.barofarm.buyer.product.application.dto.ProductCreateCommand;
import com.barofarm.buyer.product.application.dto.ProductDetailInfo;
import com.barofarm.buyer.product.application.dto.ProductImageUpdateMode;
import com.barofarm.buyer.product.application.dto.ProductInventoryOptionCommand;
import com.barofarm.buyer.product.application.dto.ProductInventoryOptionInfo;
import com.barofarm.buyer.product.application.dto.ProductUpdateCommand;
import com.barofarm.buyer.product.application.event.ProductTransactionEvent;
import com.barofarm.buyer.product.domain.Category;
import com.barofarm.buyer.product.domain.CategoryRepository;
import com.barofarm.buyer.product.domain.Product;
import com.barofarm.buyer.product.domain.ProductRepository;
import com.barofarm.buyer.product.domain.ProductStatus;
import com.barofarm.buyer.product.domain.ReviewSummary;
import com.barofarm.buyer.product.domain.ReviewSummaryRepository;
import com.barofarm.buyer.product.domain.ReviewSummarySentiment;
import com.barofarm.buyer.product.domain.UserType;
import com.barofarm.buyer.product.exception.ProductErrorCode;
import com.barofarm.dto.CustomPage;
import com.barofarm.exception.CustomException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryService inventoryService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ReviewSummaryRepository reviewSummaryRepository;
    private final ProductImageService productImageService;

    @Transactional(readOnly = true)
    public ProductDetailInfo getProductDetail(UUID id) {
        Product product =
            productRepository.findById(id)
            .orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));

        List<Inventory> inventories = inventoryService.getInventoriesByProductId(id);
        List<ProductInventoryOptionInfo> inventoryOptions = toInventoryOptionInfos(inventories);

        List<String> positiveSummary = fetchSummaryLines(id, ReviewSummarySentiment.POSITIVE);
        List<String> negativeSummary = fetchSummaryLines(id, ReviewSummarySentiment.NEGATIVE);

        return ProductDetailInfo.from(product, inventoryOptions, positiveSummary, negativeSummary);
    }

    @Transactional(readOnly = true)
    public CustomPage<ProductDetailInfo> getProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);

        Page<ProductDetailInfo> details = products.map(product -> {
            List<Inventory> inventories = inventoryService.getInventoriesByProductId(product.getId());
            List<ProductInventoryOptionInfo> inventoryOptions = toInventoryOptionInfos(inventories);
            return ProductDetailInfo.from(product, inventoryOptions);
        });

        return CustomPage.from(details);
    }

    @Transactional
    public ProductDetailInfo createProduct(ProductCreateCommand command, List<MultipartFile> images) {
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

        if (images != null && !images.isEmpty()) {
            productImageService.createProductImagesSafely(product, images);
        }

        // 상품 저장 및 재고 저장
        Product savedProduct = saveProductAndInventory(product, command.inventoryOptions());

        List<ProductInventoryOptionInfo> inventoryOptions =
            toInventoryOptionInfos(inventoryService.getInventoriesByProductId(savedProduct.getId()));
        return ProductDetailInfo.from(savedProduct, inventoryOptions);
    }

    public Product saveProductAndInventory(
        Product product,
        List<ProductInventoryOptionCommand> inventoryOptions
    ) {
        Product savedProduct = productRepository.save(product);

        //재고 생성
        for (ProductInventoryOptionCommand option : safeInventoryOptions(inventoryOptions)) {
            inventoryService.createInventory(
                new InventoryCreateCommand(
                    savedProduct.getId(),
                    option.quantity(),
                    option.unit()
                )
            );
        }

        // 트랜잭션 이벤트 발행 (트랜잭션 성공 시에만 카프카 이벤트 발행됨)
        ProductTransactionEvent event = new ProductTransactionEvent(savedProduct,
                ProductTransactionEvent.ProductOperation.CREATED);
        applicationEventPublisher.publishEvent(event);

        return savedProduct;
    }

    @Transactional
    public ProductDetailInfo updateProduct(
        UUID id,
        ProductUpdateCommand command,
        List<MultipartFile> images,
        ProductImageUpdateMode imageUpdateMode
    ) {
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

        if (imageUpdateMode != null) {
            productImageService.updateProductImagesSafely(product, images, imageUpdateMode);
        }

        Product savedProduct = updateProductAndInventory(product, command.inventoryOptions());
        List<ProductInventoryOptionInfo> inventoryOptions =
            toInventoryOptionInfos(inventoryService.getInventoriesByProductId(savedProduct.getId()));
        return ProductDetailInfo.from(savedProduct, inventoryOptions);
    }

    public Product updateProductAndInventory(
        Product product,
        List<ProductInventoryOptionCommand> inventoryOptions
    ) {
        Product updatedProduct = productRepository.save(product);

        //재고 업데이트 로직
        inventoryService.replaceInventories(
            updatedProduct.getId(),
            toInventoryCreateCommands(updatedProduct.getId(), inventoryOptions)
        );

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

        inventoryService.deleteInventoriesByProductId(id);
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

    private List<String> fetchSummaryLines(UUID productId, ReviewSummarySentiment sentiment) {
        Optional<ReviewSummary> summary =
            reviewSummaryRepository.findByProductIdAndSentiment(productId, sentiment);
        return summary
            .map(ReviewSummary::getSummaryText)
            .orElse(List.of());
    }

    private List<ProductInventoryOptionCommand> safeInventoryOptions(
        List<ProductInventoryOptionCommand> inventoryOptions
    ) {
        return inventoryOptions == null ? Collections.emptyList() : inventoryOptions;
    }

    private List<ProductInventoryOptionInfo> toInventoryOptionInfos(List<Inventory> inventories) {
        return inventories == null
            ? List.of()
            : inventories.stream()
                .map(inventory -> new ProductInventoryOptionInfo(
                    inventory.getId(),
                    inventory.getQuantity(),
                    inventory.getUnit()
                ))
                .toList();
    }

    private List<InventoryCreateCommand> toInventoryCreateCommands(
        UUID productId,
        List<ProductInventoryOptionCommand> inventoryOptions
    ) {
        return safeInventoryOptions(inventoryOptions).stream()
            .map(option -> new InventoryCreateCommand(
                productId,
                option.quantity(),
                option.unit()
            ))
            .toList();
    }
}
