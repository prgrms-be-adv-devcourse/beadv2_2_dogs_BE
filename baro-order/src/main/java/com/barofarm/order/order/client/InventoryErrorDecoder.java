package com.barofarm.order.order.client;

import com.barofarm.order.common.exception.CustomException;
import com.barofarm.order.order.exception.OrderErrorCode;
import feign.Response;
import feign.codec.ErrorDecoder;

public class InventoryErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();

        return switch (status) {
            case 404 -> new CustomException(OrderErrorCode.PRODUCT_NOT_FOUND);
            case 409 -> new CustomException(OrderErrorCode.OUT_OF_STOCK);
            default -> new CustomException(OrderErrorCode.INVENTORY_SERVICE_ERROR);
        };
    }
}
