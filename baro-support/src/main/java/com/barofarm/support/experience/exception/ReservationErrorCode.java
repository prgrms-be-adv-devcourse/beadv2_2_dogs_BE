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
    EXPERIENCE_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "현재 예약 가능한 상태가 아닙니다."),
    INVALID_RESERVATION_DATE(HttpStatus.BAD_REQUEST, "예약 가능한 기간이 아닙니다."),
    INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "유효하지 않은 상태 변경입니다."),
    STATUS_CANNOT_BE_CHANGED(HttpStatus.BAD_REQUEST, "이미 완료 상태로 변경되어 더 이상 변경할 수 없습니다."),
    RESERVATION_CANNOT_BE_DELETED(HttpStatus.BAD_REQUEST, "해당 상태의 예약은 삭제할 수 없습니다. 취소만 가능합니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 예약에 대한 접근 권한이 없습니다.");

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
