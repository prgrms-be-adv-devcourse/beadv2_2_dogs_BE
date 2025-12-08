package com.barofarm.buyer.common.exception;

import org.springframework.http.HttpStatus;

public interface BaseErrorCode {
  HttpStatus getHttpStatus();

  String getMessage();
}
