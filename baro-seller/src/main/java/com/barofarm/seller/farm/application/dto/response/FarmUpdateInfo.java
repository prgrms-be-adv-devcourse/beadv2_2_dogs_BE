package com.barofarm.seller.farm.application.dto.response;

import com.barofarm.seller.farm.domain.Farm;
import com.barofarm.seller.farm.domain.Status;

import java.util.UUID;

public record FarmUpdateInfo(
    UUID id,
    String name,
    String description,
    String address,
    String phone,
    Status status,
    UUID sellerId
) {
    public static FarmUpdateInfo from(Farm farm) {
        return new FarmUpdateInfo(
            farm.getId(),
            farm.getName(),
            farm.getDescription(),
            farm.getAddress(),
            farm.getPhone(),
            farm.getStatus(),
            farm.getSeller().getId());
    }
}
