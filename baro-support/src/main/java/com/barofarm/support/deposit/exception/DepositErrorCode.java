package com.barofarm.support.deposit.exception;

import com.barofarm.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DepositErrorCode implements BaseErrorCode {

    DEPOSIT_CHARGE_NOT_FOUND(HttpStatus.NOT_FOUND, "예치금 충전 요청을 찾을 수 없습니다."),
    DEPOSIT_NOT_FOUND(HttpStatus.NOT_FOUND, "예치금 계정을 찾을 수 없습니다."),
    DEPOSIT_CHARGE_INVALID_STATUS(HttpStatus.CONFLICT, "현재 상태에서 처리할 수 없는 예치금 충전 요청입니다."),
    INSUFFICIENT_DEPOSIT_BALANCE(HttpStatus.BAD_REQUEST, "예치금 잔액이 부족합니다."),
    DEPOSIT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "다른 사용자의 예치금 계정에는 접근할 수 없습니다."),
    DEPOSIT_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 예치금 계정이 존재합니다."),
    DEPOSIT_PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "예치금 결제 이력을 찾을 수 없습니다."),
    OUTBOX_SERIALIZATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "예치금 이벤트 직렬화에 실패했습니다.");

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
