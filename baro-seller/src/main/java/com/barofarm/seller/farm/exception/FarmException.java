package com.barofarm.seller.farm.exception;

import lombok.Getter;

@Getter
public class FarmException extends RuntimeException {
    private final FarmErrorCode errorCode;

    public FarmException(FarmErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
