package com.barofarm.buyer.cart.exception;

import com.barofarm.buyer.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CartErrorCode implements BaseErrorCode {

    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 장바구니입니다."),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니에 해당 상품이 없습니다."),
    BUYER_ID_OR_SESSION_KEY_REQUIRED(HttpStatus.BAD_REQUEST, "buyerId 또는 sessionKey가 필요합니다."),
    QUANTITY_MUST_BE_POSITIVE(HttpStatus.BAD_REQUEST, "수량은 1 이상이어야 합니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
