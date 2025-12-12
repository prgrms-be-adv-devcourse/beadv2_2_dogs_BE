package com.barofarm.order.order.domain;

public enum OrderStatus {
    PENDING,   // 결제 대기
    PAID,      // 결제 완료
    CANCELED   // 결제 실패/취소
}
