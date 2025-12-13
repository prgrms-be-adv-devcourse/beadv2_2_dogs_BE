package com.barofarm.buyer.inventory.exception;

import com.barofarm.buyer.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InventoryErrorCode implements BaseErrorCode {

    INVENTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 상품의 재고 정보가 존재하지 않습니다."),
    INSUFFICIENT_STOCK(HttpStatus.CONFLICT, "재고가 부족합니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "재고 차감을 위한 요청 값이 올바르지 않습니다.");

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
