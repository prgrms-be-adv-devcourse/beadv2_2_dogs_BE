package com.barofarm.support.delivery.exception;

import com.barofarm.support.common.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum DeliveryErrorCode implements BaseErrorCode {

    DELIVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "배송을 찾을 수가 없습니다"),
    DELIVERY_ALREADY_EXISTS(HttpStatus.CONFLICT,"이미 배송이 생성된 주문입니다.");

    private HttpStatus status;
    private String message;
}
