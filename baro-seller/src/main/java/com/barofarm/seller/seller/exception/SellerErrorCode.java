package com.barofarm.seller.seller.exception;

import com.barofarm.seller.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SellerErrorCode implements BaseErrorCode {

    SELLER_NOT_FOUND(HttpStatus.NOT_FOUND, "Seller not found."),
    SELLER_ALREADY_EXISTS(HttpStatus.CONFLICT, "Seller profile already exists.");

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
