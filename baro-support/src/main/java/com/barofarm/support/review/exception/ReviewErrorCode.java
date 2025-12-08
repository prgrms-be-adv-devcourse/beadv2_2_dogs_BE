package com.barofarm.support.review.exception;

import com.barofarm.support.common.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ReviewErrorCode implements BaseErrorCode {

    ORDER_ITEM_ID_NULL(HttpStatus.BAD_REQUEST, "OrderItemId는 null이면 안됩니다"),
    BUYER_ID_NULL(HttpStatus.BAD_REQUEST, "BuyerId는 null이면 안됩니다"),
    PRODUCT_ID_NULL(HttpStatus.BAD_REQUEST, "ProductId는 null이면 안됩니다"),
    RATING_NULL(HttpStatus.BAD_REQUEST, "Rating는 null이면 안됩니다"),
    STATUS_NULL(HttpStatus.BAD_REQUEST, "Status는 null이면 안됩니다"),

    INVALID_RATING_VALUE(HttpStatus.BAD_REQUEST, "별점은 1에서 5사이의 정수여야 합니다");

    private HttpStatus status;
    private String message;
}
