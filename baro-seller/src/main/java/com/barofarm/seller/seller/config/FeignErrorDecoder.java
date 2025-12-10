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

        if (status == HttpStatus.GATEWAY_TIMEOUT) {
            return new CustomException(FeignErrorCode.AUTH_SERVICE_TIMEOUT);
        }

        if (status == HttpStatus.BAD_GATEWAY || status == HttpStatus.SERVICE_UNAVAILABLE) {
            return new CustomException(FeignErrorCode.AUTH_SERVICE_UNAVAILABLE);
        }

        if (status.is5xxServerError()) {
            return new CustomException(FeignErrorCode.AUTH_SERVICE_ERROR);
        }

        return defaultDecoder.decode(methodKey, response);
    }
}
