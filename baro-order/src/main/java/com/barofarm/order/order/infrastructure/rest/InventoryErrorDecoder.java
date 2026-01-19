package com.barofarm.order.order.infrastructure.rest;

import com.barofarm.exception.CustomException;
import com.barofarm.order.order.exception.OrderErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InventoryErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        log.warn("InventoryErrorDecoder methodKey={}, status={}", methodKey, status);

        // 404, 409 등 비즈니스 에러 → 바로 매핑
        if (status == 404) {
            return new CustomException(OrderErrorCode.INVENTORY_NOT_FOUND);
        } else if (status == 409) {
            return new CustomException(OrderErrorCode.OUT_OF_STOCK);
        } else if (status >= 500) {
            // 5xx → 재시도
            return new RetryableException(
                status,
                "Retryable inventory error. status=" + status,
                response.request().httpMethod(),
                null,
                (Long) null,
                response.request()
            );
        }

        return new CustomException(OrderErrorCode.INVENTORY_SERVICE_ERROR);
    }
}
