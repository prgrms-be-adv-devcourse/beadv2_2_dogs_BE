package com.barofarm.seller.farm.application.dto.request;

public record FarmCreateCommand(
    String name,
    String description,
    String address,
    String phone
) {
}
