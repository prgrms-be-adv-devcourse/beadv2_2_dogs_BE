package com.barofarm.order.order.client;

import static com.barofarm.order.order.exception.OrderErrorCode.*;

import com.barofarm.order.common.exception.CustomException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class InventoryErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();

        return switch (status) {
            case 404 -> new CustomException(PRODUCT_NOT_FOUND);
            case 409 -> new CustomException(OUT_OF_STOCK);
            default -> new CustomException(INVENTORY_SERVICE_ERROR);
        };
    }
}
