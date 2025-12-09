package com.barofarm.support.experience.exception;

import com.barofarm.support.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReservationErrorCode implements BaseErrorCode {

    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 예약 정보입니다."),
    CAPACITY_EXCEEDED(HttpStatus.BAD_REQUEST, "해당 시간대의 예약 가능 인원을 초과했습니다."),
    EXPERIENCE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 체험 프로그램입니다.");

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
