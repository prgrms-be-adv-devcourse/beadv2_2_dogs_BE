package com.barofarm.support.review.application;

import com.barofarm.support.common.exception.CustomException;
import com.barofarm.support.common.response.CustomPage;
import com.barofarm.support.review.application.dto.request.ReviewCreateCommand;
import com.barofarm.support.review.application.dto.request.ReviewUpdateCommand;
import com.barofarm.support.review.application.dto.response.ReviewDetailInfo;
import com.barofarm.support.review.client.OrderClient;
import com.barofarm.support.review.client.ProductClient;
import com.barofarm.support.review.client.dto.OrderItemResponse;
import com.barofarm.support.review.client.dto.OrderStatus;
import com.barofarm.support.review.client.dto.ProductResponse;
import com.barofarm.support.review.client.dto.ProductStatus;
import com.barofarm.support.review.domain.Review;
import com.barofarm.support.review.domain.ReviewRepository;
import com.barofarm.support.review.domain.ReviewStatus;
import com.barofarm.support.review.exception.ReviewErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final OrderClient orderClient;
    private final ProductClient productClient;
    private final ReviewRepository reviewRepository;

    @Transactional
    public ReviewDetailInfo createReview(ReviewCreateCommand command) {
        // 1. 주문 정보 조회
        OrderItemResponse item = getOrderItem(command.orderItemId());

        // 2. 구매자 검증
        validateOrderOwner(item.buyerId(), command.buyerId());

        // 3. 주문 상태 검증(order의 주문 상태가 review 가능한 상태인지 확인)
        validateOrderStatus(item.status());

        // 4. 상품 조회
        ProductResponse product = getProduct(command.productId());

        // 5. 상품 상태 검증(상품의 상태가 review 가능해야 함)
        validateProductStatus(product.status());

        // 6. 중복 리뷰 방지(중복된 리뷰가 없는지 확인)
        validateDuplicateReview(command.orderItemId());

        // 7. 리뷰 엔티티 생성
        Review review = Review.create(
            command.orderItemId(),
            command.buyerId(),
            command.productId(),
            command.rating(),
            command.reviewStatus(),
            command.content()
        );

        // 8. 저장
        Review saved = reviewRepository.save(review);
        return ReviewDetailInfo.from(saved);
    }

    @Transactional(readOnly = true)
    public ReviewDetailInfo getReviewDetail(UUID reviewId, UUID userId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new CustomException(ReviewErrorCode.REVIEW_NOT_FOUND));

        // 리뷰 읽을 수 있는 권한이 있는지 검증
        review.validateReadableBy(userId);

        return ReviewDetailInfo.from(review);
    }

    @Transactional(readOnly = true)
    public CustomPage<ReviewDetailInfo> getReviewByProductId(UUID productId, Pageable pageable) {
        Page<ReviewDetailInfo> reviews =
            reviewRepository.findByProductIdAndStatusIn(
                    productId,
                    ReviewStatus.getVisibleToPublicSet(),
                    pageable
                )
                .map(ReviewDetailInfo::from);

        return CustomPage.from(reviews);
    }

    @Transactional(readOnly = true)
    public CustomPage<ReviewDetailInfo> getReviewByBuyerId(UUID loginUserId,
                                                           Pageable pageable) {
        Page<ReviewDetailInfo> reviews =
            reviewRepository.findByBuyerIdAndStatusIn(
                loginUserId,
                ReviewStatus.getVisibleToOwnerSet(),
                pageable
                )
            .map(ReviewDetailInfo::from);

        return CustomPage.from(reviews);
    }

    @Transactional
    public ReviewDetailInfo updateReview(ReviewUpdateCommand command) {
        Review review = reviewRepository.findById(command.reviewId())
            .orElseThrow(() -> new CustomException(ReviewErrorCode.REVIEW_NOT_FOUND));

        // 리뷰 작성자 검증
        review.validateReviewOwner(command.buyerId());

        // 리뷰 상태 검증
        review.validateUserUpdatable();

        review.update(
            command.rating(),
            command.reviewStatus(),
            command.content()
        );

        return ReviewDetailInfo.from(review);
    }

    @Transactional
    public void deleteReview(UUID reviewId, UUID buyerId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new CustomException(ReviewErrorCode.REVIEW_NOT_FOUND));

        // 리뷰 작성자 검증
        review.validateReviewOwner(buyerId);

        // 소프트 삭제(delete)
        review.delete();
    }


    private OrderItemResponse getOrderItem(UUID orderItemId) {
        return orderClient.getOrderItem(orderItemId);
    }

    private ProductResponse getProduct(UUID productId) {
        return productClient.getProduct(productId);
    }

    private void validateOrderOwner(UUID ownerId, UUID requesterId) {
        if (!ownerId.equals(requesterId)) {
            throw new CustomException(ReviewErrorCode.ORDER_NOT_OWNED_BY_USER);
        }
    }

    private void validateOrderStatus(String status) {
        OrderStatus orderStatus = OrderStatus.from(status);
        if (orderStatus == null || orderStatus.isNotReviewable()) {
            throw new CustomException(ReviewErrorCode.ORDER_NOT_COMPLETED);
        }
    }

    private void validateProductStatus(String status) {
        ProductStatus productStatus = ProductStatus.from(status);
        if (productStatus == null || productStatus.isNotReviewable()) {
            throw new CustomException(ReviewErrorCode.INVALID_PRODUCT_STATUS);
        }
    }

    private void validateDuplicateReview(UUID orderItemId) {
        if (reviewRepository.existsByOrderItemId(orderItemId)) {
            throw new CustomException(ReviewErrorCode.DUPLICATE_REVIEW);
        }
    }
}
