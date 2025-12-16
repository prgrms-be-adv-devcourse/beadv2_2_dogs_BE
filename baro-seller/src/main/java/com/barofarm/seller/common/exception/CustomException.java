package com.barofarm.seller.common.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final BaseErrorCode errorCode;
    private final String customMessage;

    public CustomException(BaseErrorCode error) {
        super(error.getMessage());
        this.errorCode = error;
        this.customMessage = null;
    }

    public CustomException(BaseErrorCode error, String customMessage) {
        super(customMessage);
        this.errorCode = error;
        this.customMessage = customMessage;
    }

    public String getCustomMessage() {
        return customMessage;
    }
}
