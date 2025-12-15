package com.barofarm.support.delivery.exception;

import com.barofarm.support.delivery.domain.DeliveryStatus;

public class InvalidDeliveryStatusException extends RuntimeException {
    public InvalidDeliveryStatusException(DeliveryStatus current) {
        super("현재 배송 상태에서는 수행할 수 없습니다. status=" + current);
    }
}
