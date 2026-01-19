package com.barofarm.payment.common.exception;

import com.barofarm.dto.ResponseDto;
import com.barofarm.exception.BaseExceptionHandler;
import feign.RetryableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler(RetryableException.class)
    public ResponseEntity<ResponseDto<Void>> handleRetryableException(RetryableException e) {

        // 외부 시스템 일시적 장애 / 타임아웃 / 네트워크 오류
        HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;

        return ResponseEntity.status(status)
            .body(ResponseDto.error(
                status,
                "외부 시스템과의 통신이 원활하지 않습니다. 잠시 후 다시 시도해주세요."
            ));
    }
}
