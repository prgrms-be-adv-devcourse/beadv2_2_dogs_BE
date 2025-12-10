package com.barofarm.seller.seller.exception;

import com.barofarm.seller.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FeignErrorCode implements BaseErrorCode {
    AUTH_SERVICE_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "Auth 서비스 호출에 실패했습니다."),
    AUTH_SERVICE_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "Auth 서비스 응답이 지연되었습니다."),
    AUTH_SERVICE_ERROR(HttpStatus.BAD_GATEWAY, "Auth 서비스 처리 중 오류가 발생했습니다.");

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
