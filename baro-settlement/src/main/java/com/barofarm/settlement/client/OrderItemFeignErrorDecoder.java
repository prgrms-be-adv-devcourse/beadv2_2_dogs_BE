package com.barofarm.settlement.client;

import com.barofarm.exception.CommonErrorCode;
import com.barofarm.exception.CustomException;
import com.barofarm.settlement.exception.SettlementErrorCode;
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
