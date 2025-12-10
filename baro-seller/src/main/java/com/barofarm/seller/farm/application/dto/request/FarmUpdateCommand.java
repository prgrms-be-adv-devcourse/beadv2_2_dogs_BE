package com.barofarm.seller.farm.application.dto.request;

public record FarmUpdateCommand(
    String name,
    String description,
    String address,
    String phone
) {
}
