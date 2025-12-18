package com.barofarm.support.delivery.domain;

public enum DeliveryStatus {
    READY,         // 배달 준비 중
    SHIPPED,       // 판매자가 발송 처리
    DELIVERING,    // 배송 중 (택배사 단계)
    DELIVERED;    // 배송 완료

    public boolean canTransitionTo(DeliveryStatus target) {
        return switch (this) {
            case DeliveryStatus.READY -> target == DeliveryStatus.SHIPPED;
            case DeliveryStatus.SHIPPED -> target == DeliveryStatus.DELIVERING;
            case DeliveryStatus.DELIVERING -> target == DeliveryStatus.DELIVERED;
            default -> false;
        };
    }
}
