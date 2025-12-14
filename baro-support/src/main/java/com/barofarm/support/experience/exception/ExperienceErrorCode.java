package com.barofarm.support.experience.exception;

import com.barofarm.support.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExperienceErrorCode implements BaseErrorCode {

    EXPERIENCE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 체험 프로그램 정보입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 체험 프로그램에 대한 접근 권한이 없습니다."),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "예약 가능 시작일은 종료일보다 이전이어야 합니다."),
    INVALID_PRICE(HttpStatus.BAD_REQUEST, "1인당 가격은 0원 이상이어야 합니다."),
    INVALID_CAPACITY(HttpStatus.BAD_REQUEST, "수용 인원은 1명 이상이어야 합니다."),
    INVALID_DURATION(HttpStatus.BAD_REQUEST, "소요 시간은 1분 이상이어야 합니다.");

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
