package com.barofarm.ai.event.model;

public enum HistoryEventType {
    // Cart Events
    CART_ITEM_ADDED,
    CART_ITEM_REMOVED,
    CART_QUANTITY_UPDATED,

    // Order Events
    ORDER_CONFIRMED,
    ORDER_CANCELLED,

    // Payment Events
    PAYMENT_CONFIRMED,
    DEPOSIT_CONFIRMED
}

