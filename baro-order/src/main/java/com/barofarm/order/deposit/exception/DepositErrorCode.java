package com.barofarm.order.deposit.exception;

import com.barofarm.order.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DepositErrorCode implements BaseErrorCode {

    DEPOSIT_CHARGE_NOT_FOUND(HttpStatus.NOT_FOUND, "예치금 충전 요청을 찾을 수 없습니다."),
    DEPOSIT_NOT_FOUND(HttpStatus.NOT_FOUND,"예치금 계정이 존재하지 않습니다."),
    DEPOSIT_CHARGE_INVALID_STATUS(HttpStatus.CONFLICT, "현재 상태에서는 예치금 충전을 처리할 수 없습니다."),
    INSUFFICIENT_DEPOSIT_BALANCE(HttpStatus.BAD_REQUEST,"예치금이 부족합니다."),
    DEPOSIT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 예치금에 대한 접근 권한이 없습니다."),
    DEPOSIT_PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND,"예치금 결제 내역을 찾을 수 없습니다.");
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
