package com.barofarm.order.order.exception;

import com.barofarm.order.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements BaseErrorCode {

    OUT_OF_STOCK(HttpStatus.CONFLICT, "재고가 부족합니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
    INVENTORY_SERVICE_ERROR(HttpStatus.BAD_GATEWAY, "재고 서비스 호출에 실패했습니다."),
    ORDER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 주문에 대한 접근 권한이 없습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 주문 정보입니다."),
    INVALID_ORDER_STATUS_FOR_PREPARING(HttpStatus.BAD_REQUEST, "결제 완료(PAID) 상태에서만 배송 준비중으로 변경할 수 있습니다."),
    ORDER_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 주문을 수정할 권한이 없습니다."),
    INVALID_ORDER_STATUS_FOR_DELIVERY(HttpStatus.CONFLICT, "배송 생성은 PAID 상태의 주문에서만 가능합니다."),
    ORDER_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 주문 상품 정보입니다.");

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
