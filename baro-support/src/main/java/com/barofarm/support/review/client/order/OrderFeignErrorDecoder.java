package com.barofarm.support.review.client.order;

import com.barofarm.support.common.exception.CommonErrorCode;
import com.barofarm.support.common.exception.CustomException;
import com.barofarm.support.review.exception.ReviewErrorCode;
import feign.Response;
import feign.codec.ErrorDecoder;

public class OrderFeignErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String s, Response response) {
        return switch (response.status()) {
            case 404 -> new CustomException(
                ReviewErrorCode.ORDER_ITEM_NOT_FOUND
            );

            default -> new CustomException(
                CommonErrorCode.FEIGN_ERROR
            );
        };
    }
}
