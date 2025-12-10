package com.barofarm.auth.exception;

import com.barofarm.auth.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ValidationErrorCode implements BaseErrorCode {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청 값 검증 실패");

    private final HttpStatus status;
    private final String message;
}
