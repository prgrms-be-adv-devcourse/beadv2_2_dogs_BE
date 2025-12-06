package com.barofarm.seller.common;

import com.barofarm.seller.farm.exception.FarmException;
import com.barofarm.seller.seller.exception.SellerException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> bindException(BindException e) {
        String message = e.getBindingResult()
            .getAllErrors()
            .get(0)
            .getDefaultMessage();

        ErrorResponse response = new ErrorResponse("400", message);

        return ResponseEntity
            .badRequest()
            .body(response);
    }

    @ExceptionHandler(FarmException.class)
    public ResponseEntity<ErrorResponse> handleFarmException(FarmException e) {
        ErrorResponse response = ErrorResponse.from(e.getErrorCode());
        return ResponseEntity
            .badRequest()
            .body(response);
    }

    @ExceptionHandler(SellerException.class)
    public ResponseEntity<ErrorResponse> handleSellerException(SellerException e) {
        ErrorResponse response = ErrorResponse.from(e.getErrorCode());
        return ResponseEntity
            .badRequest()
            .body(response);
    }
}
