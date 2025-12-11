package com.barofarm.order.payment.exception;

import com.barofarm.order.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements BaseErrorCode {

    INVALID_SECRET_KEY(HttpStatus.UNAUTHORIZED, "유효한 Toss Secret Key가 설정되어 있지 않습니다."),;

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
