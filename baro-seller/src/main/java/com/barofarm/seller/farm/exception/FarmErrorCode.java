package com.barofarm.seller.farm.exception;

import com.barofarm.seller.common.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FarmErrorCode implements BaseErrorCode {

    FARM_NOT_FOUND("400_1", "존재하지 않는 농장 정보입니다.");

    private final String code;
    private final String message;
}
