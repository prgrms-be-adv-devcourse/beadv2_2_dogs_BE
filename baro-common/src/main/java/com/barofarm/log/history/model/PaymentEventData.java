package com.barofarm.log.history.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEventData {
    private UUID paymentId;
    private UUID orderId;
    private UUID userId;
    private Long amount;
    private String purpose; // ORDER_PAYMENT, DEPOSIT_CHARGE 등
}

