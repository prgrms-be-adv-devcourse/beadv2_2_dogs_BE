package com.barofarm.seller.seller.exception;

import com.barofarm.seller.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ValidationErrorCode implements BaseErrorCode {
    // 필수값 누락
    REQUIRED_FIELD_MISSING(HttpStatus.BAD_REQUEST, "사업자번호와 대표자명은 필수입니다."),

    // 형식 오류(길이)
    INVALID_BUSINESS_NO_FORMAT(HttpStatus.BAD_REQUEST, "사업자등록번호 형식이 올바르지 않습니다.."),

    // 중복 리소스
    DUPLICATE_BUSINESS_NO(HttpStatus.CONFLICT, "이미 등록된 사업자등록번호입니다."),

    // 데이터 없음(조회 시)
    SELLER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 판매자 정보를 찾을 수 없습니다."),

    // 예기치 않은 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

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
