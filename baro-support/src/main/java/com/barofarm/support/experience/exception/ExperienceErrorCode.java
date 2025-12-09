package com.barofarm.support.experience.exception;

import com.barofarm.support.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExperienceErrorCode implements BaseErrorCode {

    EXPERIENCE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 체험 프로그램 정보입니다.");

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
