package com.barofarm.support.review.application;

import com.barofarm.support.common.exception.CustomException;
import com.barofarm.support.common.response.CustomPage;
import com.barofarm.support.review.application.dto.request.ReviewCreateCommand;
import com.barofarm.support.review.application.dto.request.ReviewUpdateCommand;
import com.barofarm.support.review.application.dto.response.ReviewDetailInfo;
import com.barofarm.support.review.client.order.OrderClient;
import com.barofarm.support.review.client.order.dto.OrderItemResponse;
import com.barofarm.support.review.client.order.dto.OrderStatus;
import com.barofarm.support.review.client.product.ProductClient;
import com.barofarm.support.review.client.product.dto.ProductResponse;
import com.barofarm.support.review.client.product.dto.ProductStatus;
import com.barofarm.support.review.domain.Review;
import com.barofarm.support.review.domain.ReviewRepository;
import com.barofarm.support.review.domain.ReviewStatus;
import com.barofarm.support.review.exception.ReviewErrorCode;
import java.util.Optional;
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

    public ReviewDetailInfo createReview(ReviewCreateCommand command) {
        // 1. 주문 정보 조회
        OrderItemResponse item = getOrderItem(command.orderItemId());

        // 2. 중복 리뷰 방지(중복된 리뷰가 없는지 확인)
        validateDuplicateReview(command.orderItemId());

        // 3. 구매자 검증(로그인한 유저 == item 구매한 사람)
        validateOrderOwner(item.buyerId(), command.userId());

        // 4. 주문 상태 검증(order의 주문 상태가 review 가능한 상태인지 확인)
        validateOrderStatus(item.status());

        // 5. 상품 조회
        ProductResponse product = getProduct(command.productId());

        // 6. 상품 상태 검증(상품의 상태가 review 가능해야 함)
        validateProductStatus(product.status());

        // 7. 리뷰 엔티티 생성
        Review review = Review.create(
            command.orderItemId(),
            command.userId(),
            command.productId(),
            command.rating(),
            command.toReviewStatus(),
            command.content()
        );

        // 8. 저장
        Review saved = savedReview(review);
        return ReviewDetailInfo.from(saved);
    }

    @Transactional
    public Review savedReview(Review review) {
        return reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public ReviewDetailInfo getReviewDetail(UUID userId, UUID reviewId) {

        Review review = findReview(reviewId);

        // 읽을 수 있는 리뷰인지 검증
        validateReadable(review, userId);

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
    public CustomPage<ReviewDetailInfo> getReviewByBuyerId(UUID userId,
                                                           Pageable pageable) {
        Page<ReviewDetailInfo> reviews =
            reviewRepository.findByBuyerIdAndStatusIn(
                userId,
                ReviewStatus.getVisibleToOwnerSet(),
                pageable
                )
            .map(ReviewDetailInfo::from);

        return CustomPage.from(reviews);
    }

    @Transactional
    public ReviewDetailInfo updateReview(ReviewUpdateCommand command) {

        Review review = findReview(command.reviewId());

        // 1. 로그인한 사용자가 리뷰 작성자가 맞는지 검증
        validateReviewOwner(review, command.userId());

        // 2. 업데이트 가능한 리뷰인지 확인
        validateReviewUpdatable(review);

        review.update(
            command.rating(),
            command.toReviewStatus(),
            command.content()
        );

        return ReviewDetailInfo.from(review);
    }

    @Transactional
    public void deleteReview(UUID userId, UUID reviewId) {
        Review review = findReview(reviewId);

        // 1. 로그인한 사용자가 리뷰 작성자가 맞는지 검증
        validateReviewOwner(review, userId);

        // 2. 삭제 가능한지 검증
        validateReviewDeletable(review);

        review.delete();
    }

    private OrderItemResponse getOrderItem(UUID orderItemId) {
        return orderClient.getOrderItem(orderItemId);
    }

    private ProductResponse getProduct(UUID productId) {
        return productClient.getProduct(productId);
    }

    private Review findReview(UUID reviewId) {
        return reviewRepository.findById(reviewId)
            .orElseThrow(() -> new CustomException(ReviewErrorCode.REVIEW_NOT_FOUND));
    }

    private void validateReadable(Review review, UUID userId) {
        if (!review.canRead(userId)) {
            throw new CustomException(ReviewErrorCode.REVIEW_FORBIDDEN);
        }
    }

    private void validateReviewOwner(Review review, UUID requesterId) {
        if (!review.isValidReviewOwner(requesterId)) {
            throw new CustomException(ReviewErrorCode.REVIEW_FORBIDDEN);
        }
    }

    private void validateReviewUpdatable(Review review) {
        if (!review.isValidUserUpdatable()) {
            throw new CustomException(ReviewErrorCode.REVIEW_NOT_UPDATABLE);
        }
    }

    private void validateReviewDeletable(Review review) {
        if (review.getStatus() == ReviewStatus.DELETED) {
            throw new CustomException(ReviewErrorCode.REVIEW_ALREADY_DELETED);
        }
    }

    private void validateOrderOwner(UUID ownerId, UUID requesterId) {
        if (!ownerId.equals(requesterId)) {
            throw new CustomException(ReviewErrorCode.ORDER_NOT_OWNED_BY_USER);
        }
    }

    private void validateOrderStatus(String status) {
        Optional<OrderStatus> orderStatus = OrderStatus.from(status);
        if (orderStatus.isEmpty() || orderStatus.get().isNotReviewable()) {
            throw new CustomException(ReviewErrorCode.ORDER_NOT_COMPLETED);
        }
    }

    private void validateProductStatus(String status) {
        Optional<ProductStatus> productStatus = ProductStatus.from(status);
        if (productStatus.isEmpty() || productStatus.get().isNotReviewable()) {
            throw new CustomException(ReviewErrorCode.INVALID_PRODUCT_STATUS);
        }
    }

    private void validateDuplicateReview(UUID orderItemId) {
        if (reviewRepository.existsByOrderItemId(orderItemId)) {
            throw new CustomException(ReviewErrorCode.DUPLICATE_REVIEW);
        }
    }
}
