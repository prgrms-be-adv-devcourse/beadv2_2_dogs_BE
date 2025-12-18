package com.barofarm.auth.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

@Schema(description = "공통 Response 포맷")
public record ResponseDto<T>(
    @Schema(description = "HTTP 상태 코드")
    int status,

    @Schema(description = "응답 데이터")
    T data,

    @Schema(description = "메시지", nullable = true)
    String message
) {

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
