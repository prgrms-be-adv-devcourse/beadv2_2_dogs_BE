package com.barofarm.buyer.inventory.exception;

import com.barofarm.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InventoryErrorCode implements BaseErrorCode {

    INVENTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 상품의 재고 정보가 존재하지 않습니다."),
    INSUFFICIENT_STOCK(HttpStatus.CONFLICT, "재고가 부족합니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "재고 차감을 위한 요청 값이 올바르지 않습니다."),
    RESERVED_QUANTITY_NOT_ENOUGH(HttpStatus.CONFLICT, "예약된 수량이 부족합니다."),
    OUTBOX_SERIALIZATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "재고 이벤트 직렬화에 실패했습니다."),
    INVENTORY_RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 상품의 재고 정보가 존재하지 않습니다."),
    ALREADY_CONFIRMED(HttpStatus.CONFLICT, "이미 확정된 재고 예약입니다."),
    ALREADY_CANCELED(HttpStatus.CONFLICT, "이미 취소된 재고 예약입니다."),
    INVENTORY_HAS_ACTIVE_RESERVATIONS(HttpStatus.CONFLICT, "해당 재고에 활성화된 예약이 있어 삭제할 수 없습니다."),
    RESERVATION_RETRY_EXCEEDED(HttpStatus.CONFLICT,"재고 예약 처리 중 동시성 충돌이 반복적으로 발생했습니다.");

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
