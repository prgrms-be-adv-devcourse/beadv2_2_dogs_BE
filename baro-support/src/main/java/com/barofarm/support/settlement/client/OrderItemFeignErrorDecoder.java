package com.barofarm.support.settlement.client;

import com.barofarm.support.common.exception.CommonErrorCode;
import com.barofarm.support.common.exception.CustomException;
import com.barofarm.support.settlement.exception.SettlementErrorCode;
import feign.Response;
import feign.codec.ErrorDecoder;

public class OrderItemFeignErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String s, Response response) {
        return switch (response.status()) {
            case 404 -> new CustomException(
                SettlementErrorCode.ORDER_ITEM_NOT_FOUND
            );

            default -> new CustomException(
                CommonErrorCode.FEIGN_ERROR
            );
        };
    }
}
