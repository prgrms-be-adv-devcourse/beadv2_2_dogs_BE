package com.barofarm.order.deposit.application.dto.request;

import java.util.UUID;

public record DepositRefundCommand(
    UUID orderId,
    long amount
) {}
