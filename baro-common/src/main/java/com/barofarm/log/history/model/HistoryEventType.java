package com.barofarm.log.history.model;

public enum HistoryEventType {
    CART_ITEM_ADDED(EventDomain.CART),
    CART_ITEM_REMOVED(EventDomain.CART),
    CART_QUANTITY_UPDATED(EventDomain.CART),
    ORDER_CONFIRMED(EventDomain.ORDER),
    ORDER_CANCELLED(EventDomain.ORDER);

    private final EventDomain domain;

    HistoryEventType(EventDomain domain) { this.domain = domain; }

    public EventDomain getDomain() {
        return domain;
    }
}
