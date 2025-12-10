package com.barofarm.support.common.exception;

import com.barofarm.support.common.response.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto<Void>> handleValidationException(
        MethodArgumentNotValidException e
    ) {
        String message = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .findFirst()
            .map(error -> error.getDefaultMessage())
            .orElse("요청 값이 올바르지 않습니다.");

        return ResponseEntity
            .badRequest()
            .body(ResponseDto.error(
                HttpStatus.BAD_REQUEST,
                message
            ));
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ResponseDto<Void>> handleCustomException(CustomException e) {
        HttpStatus status = e.getErrorCode().getStatus();

        return ResponseEntity.status(status)
            .body(ResponseDto.error(status, e.getErrorCode().getMessage()));
    }
}
