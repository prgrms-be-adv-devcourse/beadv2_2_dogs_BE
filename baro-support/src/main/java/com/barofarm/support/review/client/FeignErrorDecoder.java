package com.barofarm.support.review.client;

import com.barofarm.support.common.exception.CommonErrorCode;
import com.barofarm.support.common.exception.CustomException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {

        return switch (response.status()) {
            case 400 -> new CustomException(CommonErrorCode.BAD_REQUEST);
            case 404 -> new CustomException(CommonErrorCode.NOT_FOUND);
            case 500 -> new CustomException(CommonErrorCode.INTERNAL_SERVER_ERROR);
            default -> new CustomException(CommonErrorCode.FEIGN_ERROR);
        };
    }
}
