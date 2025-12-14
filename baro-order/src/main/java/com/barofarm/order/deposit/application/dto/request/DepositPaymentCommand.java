package com.barofarm.order.deposit.application.dto.request;

import java.util.UUID;

public record DepositPaymentCommand(
    UUID orderId,
    long amount
) {}
