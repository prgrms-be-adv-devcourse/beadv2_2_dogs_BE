package com.barofarm.support.review.exception;

import com.barofarm.support.common.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum ReviewErrorCode implements BaseErrorCode {

    // ======== ORDER 검증 관련 ========
    ORDER_ITEM_ID_NULL(HttpStatus.BAD_REQUEST, "OrderItemId는 null이면 안됩니다"),
    ORDER_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "OrderItem을 찾을 수 없습니다"),
    ORDER_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "배송 완료된 주문만 리뷰를 작성할 수 있습니다."),
    ORDER_NOT_OWNED_BY_USER(HttpStatus.FORBIDDEN, "해당 주문의 구매자가 아닙니다."),
    INVALID_ORDER_STATUS(HttpStatus.BAD_REQUEST, "주문의 상태가 완료여야 리뷰를 작성할 수 있습니다"),

    // ======== PRODUCT 검증 관련 ========
    PRODUCT_ID_NULL(HttpStatus.BAD_REQUEST, "ProductId는 null이면 안됩니다"),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다"),
    INVALID_PRODUCT_STATUS(HttpStatus.BAD_REQUEST, "상품의 상태가 온전치 않습니다"),

    // ======== REVIEW 검증 관련 ========
    BUYER_ID_NULL(HttpStatus.BAD_REQUEST, "BuyerId는 null이면 안됩니다"),
    RATING_NULL(HttpStatus.BAD_REQUEST, "Rating는 null이면 안됩니다"),
    STATUS_NULL(HttpStatus.BAD_REQUEST, "Status는 null이면 안됩니다"),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."),

    DUPLICATE_REVIEW(HttpStatus.BAD_REQUEST, "이미 해당 주문에 대한 리뷰가 존재합니다."),
    INVALID_RATING_VALUE(HttpStatus.BAD_REQUEST, "별점은 1에서 5사이의 정수여야 합니다"),
    REVIEW_NOT_UPDATABLE(HttpStatus.CONFLICT, "현재 상태의 리뷰는 수정할 수 없습니다."),
    REVIEW_FORBIDDEN(HttpStatus.BAD_REQUEST, "리뷰의 소유자가 아닙니다"),
    REVIEW_ALREADY_DELETED(HttpStatus.CONFLICT, "이미 삭제된 리뷰입니다."),
    REVIEW_NOT_READABLE(HttpStatus.FORBIDDEN, "해당 리뷰를 조회할 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    ReviewErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
