package com.barofarm.auth.exception;

import com.barofarm.auth.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum VerificationErrorCode implements BaseErrorCode {
    CODE_NOT_FOUND(HttpStatus.BAD_REQUEST, "이메일 인증 코드를 찾을 수 없습니다."),
    CODE_EXPIRED(HttpStatus.BAD_REQUEST, "인증 코드가 만료되었습니다."),
    VERIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "이메일 인증 이력이 없습니다."),
    VERIFICATION_NOT_COMPLETED(HttpStatus.UNAUTHORIZED, "이메일 인증이 완료되지 않았습니다.");

    private final HttpStatus status;
    private final String message;
}
