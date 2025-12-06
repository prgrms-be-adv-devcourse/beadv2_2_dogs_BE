package com.barofarm.seller.seller.exception;

import com.barofarm.seller.common.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SellerErrorCode implements BaseErrorCode {

    SELLER_NOT_FOUND("400_1", "존재하지않는 판매자 정보입니다.");

    private final String code;
    private final String message;
}
