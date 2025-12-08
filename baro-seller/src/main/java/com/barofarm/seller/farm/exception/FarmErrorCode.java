package com.barofarm.seller.farm.exception;

import com.barofarm.seller.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FarmErrorCode implements BaseErrorCode {

    FARM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 농장 정보입니다.");

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
