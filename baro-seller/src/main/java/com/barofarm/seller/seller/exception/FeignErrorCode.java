package com.barofarm.seller.seller.exception;

import com.barofarm.seller.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FeignErrorCode implements BaseErrorCode {
    // 4xx: 클라이언트 잘못된 요청/인증 문제
    AUTH_SERVICE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "Auth 서비스로 보낸 요청이 유효하지 않습니다."),
    AUTH_SERVICE_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Auth 인증/인가가 필요합니다."),
    AUTH_SERVICE_FORBIDDEN(HttpStatus.FORBIDDEN, "Auth 서비스 접근이 거부되었습니다."),
    AUTH_SERVICE_NOT_FOUND(HttpStatus.NOT_FOUND, "Auth 서비스에서 리소스를 찾을 수 없습니다."),
    AUTH_SERVICE_CLIENT_ERROR(HttpStatus.BAD_REQUEST, "Auth 서비스 클라이언트 오류가 발생했습니다."),

    // 5xx: 서버/네트워크 문제
    AUTH_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "Auth 서비스 호출에 실패했습니다."),
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
