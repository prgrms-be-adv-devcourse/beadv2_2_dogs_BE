package com.barofarm.support.delivery.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum DeliveryErrorCode {

    DELIVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "배송을 찾을 수가 없습니다");

    private HttpStatus status;
    private String message;
}
