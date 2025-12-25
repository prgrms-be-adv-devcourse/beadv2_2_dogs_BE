package com.barofarm.buyer.common.exception;

import org.springframework.http.HttpStatus;

public interface BaseErrorCode {
    HttpStatus getStatus();

    String getMessage();
}
