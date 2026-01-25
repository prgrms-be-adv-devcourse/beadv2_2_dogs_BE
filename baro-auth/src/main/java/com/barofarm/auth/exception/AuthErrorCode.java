package com.barofarm.auth.exception;

import com.barofarm.exception.BaseErrorCode;
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
    CURRENT_PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "현재 비밀번호가 올바르지 않습니다."),
    OAUTH_INVALID_STATE(HttpStatus.UNAUTHORIZED, "OAuth state가 유효하지 않습니다."),
    OAUTH_LINK_REQUIRED(HttpStatus.CONFLICT, "이미 동일 이메일의 로컬 계정이 존재합니다. 연결 절차가 필요합니다."),
    OAUTH_ALREADY_LINKED(HttpStatus.CONFLICT, "해당 소셜 계정이 다른 사용자에게 이미 연결되어 있습니다."),
    OAUTH_UNSUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST, "지원하지 않는 OAuth 공급자입니다."),
    OAUTH_EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, "OAuth 로그인을 위해 이메일 제공이 필요합니다.");

    private final HttpStatus status;
    private final String message;
}
