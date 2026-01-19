package com.barofarm.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final BaseErrorCode errorCode;

    public CustomException(BaseErrorCode error) {
        super(error.getMessage());
        this.errorCode = error;
    }
}
