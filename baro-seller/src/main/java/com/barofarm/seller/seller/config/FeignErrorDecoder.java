package com.barofarm.seller.seller.config;

import com.barofarm.seller.common.exception.CustomException;
import com.barofarm.seller.seller.exception.FeignErrorCode;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus status = HttpStatus.resolve(response.status());

        if (status == null) {
            return defaultDecoder.decode(methodKey, response);
        }

        log.error("[Feign] {} call failed: status={}, reason={}", methodKey, status.value(), response.reason());

        // 4xx: 클라이언트 요청 문제를 명시적으로 매핑
        if (status.is4xxClientError()) {
            if (status == HttpStatus.BAD_REQUEST) {
                return new CustomException(FeignErrorCode.AUTH_SERVICE_BAD_REQUEST); // 파라미터/본문 검증 실패 등
            }
            if (status == HttpStatus.UNAUTHORIZED || status == HttpStatus.FORBIDDEN) {
                return new CustomException(FeignErrorCode.AUTH_SERVICE_UNAUTHORIZED); // 인증/권한 오류
            }
            if (status == HttpStatus.NOT_FOUND) {
                return new CustomException(FeignErrorCode.AUTH_SERVICE_NOT_FOUND); // 대상 리소스 없음
            }
            return new CustomException(FeignErrorCode.AUTH_SERVICE_CLIENT_ERROR); // 기타 4xx
        }

        // 504: 타임아웃
        if (status == HttpStatus.GATEWAY_TIMEOUT) {
            return new CustomException(FeignErrorCode.AUTH_SERVICE_TIMEOUT);
        }

        // 502/503: 서버/네트워크 불가
        if (status == HttpStatus.BAD_GATEWAY || status == HttpStatus.SERVICE_UNAVAILABLE) {
            return new CustomException(FeignErrorCode.AUTH_SERVICE_UNAVAILABLE);
        }

        // 기타 5xx
        if (status.is5xxServerError()) {
            return new CustomException(FeignErrorCode.AUTH_SERVICE_ERROR);
        }

        return defaultDecoder.decode(methodKey, response);
    }
}
