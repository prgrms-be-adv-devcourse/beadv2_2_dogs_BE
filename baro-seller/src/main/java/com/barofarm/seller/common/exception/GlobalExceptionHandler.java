package com.barofarm.seller.common.exception;

import com.barofarm.seller.common.response.ResponseDto;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ResponseDto<Void>> handleBindException(BindException e) {
        String message = e.getBindingResult()
            .getAllErrors()
            .get(0)
            .getDefaultMessage();

        ResponseDto<Void> body = ResponseDto.error(HttpStatus.BAD_REQUEST, message);

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(body);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ResponseDto<Void>> handleCustomException(CustomException e) {
        HttpStatus status = e.getErrorCode().getStatus();

        return ResponseEntity.status(status)
            .body(ResponseDto.error(status, e.getErrorCode().getMessage()));
    }

    // 컨트롤러 메서드 파라미터 안맞을 때 처리 하는것 추가 [1216-상현]
    @ExceptionHandler({ MethodArgumentTypeMismatchException.class, ConversionFailedException.class })
    public ResponseEntity<ResponseDto<Void>> handleTypeMismatch(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ResponseDto.error(HttpStatus.BAD_REQUEST, "잘못된 파라미터 형식: " + e.getMessage()));
    }

}
