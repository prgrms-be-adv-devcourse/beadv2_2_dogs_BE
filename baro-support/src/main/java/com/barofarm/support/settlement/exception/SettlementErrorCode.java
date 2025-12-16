package com.barofarm.support.settlement.exception;

import com.barofarm.support.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SettlementErrorCode implements BaseErrorCode {

    SETTLEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 월의 정산 내역이 존재하지 않습니다."),
    ORDER_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "주문 상품을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
