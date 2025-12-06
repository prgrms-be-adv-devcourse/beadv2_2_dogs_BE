package com.barofarm.seller.seller.exception;

import lombok.Getter;

@Getter
public class SellerException extends RuntimeException {
    private final SellerErrorCode errorCode;

    public SellerException(SellerErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
