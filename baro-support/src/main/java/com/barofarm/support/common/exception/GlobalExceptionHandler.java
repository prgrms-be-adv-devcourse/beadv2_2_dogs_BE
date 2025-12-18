package com.barofarm.support.common.exception;

import com.barofarm.support.common.response.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
}
