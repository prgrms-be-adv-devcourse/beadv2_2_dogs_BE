package com.barofarm.order.order.domain;

public enum OrderStatus {
    PENDING,
    PAID,
    PREPARING,
    SHIPPED,
    CANCELED,
    COMPLETED;

    public boolean isCancelable() {
        return this == PENDING || this == PAID;
    }
}
