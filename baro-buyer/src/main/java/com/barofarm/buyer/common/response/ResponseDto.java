package com.barofarm.buyer.common.response;

import org.springframework.http.HttpStatus;

public record ResponseDto<T>(int status, T data, String message) {
    public ResponseDto(HttpStatus status, T data, String message) {
        this(status.value(), data, message);
    }

  public static <T> ResponseDto<T> ok(T data) {
    return new ResponseDto<>(HttpStatus.OK, data, null);
  }

    public static <T> ResponseDto<T> error(HttpStatus status, String message) {
        return new ResponseDto<>(status, null, message);
    }
}
