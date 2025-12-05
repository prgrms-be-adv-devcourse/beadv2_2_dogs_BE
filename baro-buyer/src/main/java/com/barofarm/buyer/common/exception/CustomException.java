package com.barofarm.buyer.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;

    public CustomException(ErrorCode error) {
        super(error.getMessage());
        this.errorCode = error;
    }
}
