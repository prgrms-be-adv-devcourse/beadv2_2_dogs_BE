package com.barofarm.buyer.common;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ResponseEntity<T> {
    private final boolean success;
    private final T data;
    private final ApiError error;

    public static <T> ResponseEntity<T> ok(T data) {
        return new ResponseEntity<>(true, data, null);
    }

    public static <T> ResponseEntity<T> error(String message, String errorCode) {
        return new ResponseEntity<>(false, null, new ApiError(message, errorCode));
    }
}
