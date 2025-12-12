package com.barofarm.order.payment.exception;

import com.barofarm.order.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements BaseErrorCode {

    INVALID_SECRET_KEY(HttpStatus.UNAUTHORIZED, "유효한 Toss Secret Key가 설정되어 있지 않습니다."),

    TOSS_PAYMENT_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "결제 요청 값이 올바르지 않습니다."),
    TOSS_PAYMENT_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Toss API 인증에 실패했습니다."),
    TOSS_PAYMENT_CONFLICT(HttpStatus.CONFLICT, "이미 처리되었거나 처리할 수 없는 결제 상태입니다."),
    TOSS_PAYMENT_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "Toss 결제 서버 오류가 발생했습니다."),
    TOSS_PAYMENT_CONFIRM_FAILED(HttpStatus.BAD_GATEWAY, "결제 승인 처리에 실패했습니다."),
    TOSS_PAYMENT_CANCEL_FAILED(HttpStatus.BAD_GATEWAY, "결제 취소 처리에 실패했습니다.");

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
