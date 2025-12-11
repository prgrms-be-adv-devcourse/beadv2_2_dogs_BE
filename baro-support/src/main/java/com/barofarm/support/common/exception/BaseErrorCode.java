package com.barofarm.support.common.exception;

import org.springframework.http.HttpStatus;

public interface BaseErrorCode {
    HttpStatus getStatus();
    String getMessage();
}
