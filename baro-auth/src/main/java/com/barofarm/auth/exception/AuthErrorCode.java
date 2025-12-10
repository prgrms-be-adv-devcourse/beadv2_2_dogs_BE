package com.barofarm.auth.exception;

import com.barofarm.auth.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
    INVALID_CREDENTIAL(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    REFRESH_TOKEN_UNUSABLE(HttpStatus.UNAUTHORIZED, "사용할 수 없는 리프레시 토큰입니다."),
    REFRESH_TOKEN_TAMPERED(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료됐거나 위조되었습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "가입된 이메일이 없습니다."),
    CREDENTIAL_NOT_FOUND(HttpStatus.NOT_FOUND, "자격 증명을 찾을 수 없습니다."),
    CURRENT_PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "현재 비밀번호가 올바르지 않습니다.");

    private final HttpStatus status;
    private final String message;
}
