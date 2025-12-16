package com.barofarm.seller.farm.exception;

import com.barofarm.seller.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FarmErrorCode implements BaseErrorCode {

    FARM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 농장 정보입니다."),
    FARM_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 농장을 수정할 권한이 없습니다."),

    FARM_IMAGE_REQUIRED(HttpStatus.BAD_REQUEST, "농장 이미지는 반드시 1장 업로드해야 합니다."),
    FARM_IMAGE_EMPTY_FILE(HttpStatus.BAD_REQUEST, "비어있는 이미지 파일이 포함되어 있습니다."),
    FARM_IMAGE_INVALID_TYPE(HttpStatus.BAD_REQUEST, "이미지 파일만 업로드 가능합니다."),
    FARM_IMAGE_TOO_LARGE(HttpStatus.BAD_REQUEST, "이미지 크기는 최대 10MB까지 가능합니다."),

    FARM_IMAGE_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "농장 이미지 업로드에 실패했습니다."),
    FARM_IMAGE_DELETE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "농장 이미지 삭제에 실패했습니다.");

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
