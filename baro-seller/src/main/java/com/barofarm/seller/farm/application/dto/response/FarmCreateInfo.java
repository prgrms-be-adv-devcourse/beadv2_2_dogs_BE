package com.barofarm.seller.farm.application.dto.response;

import com.barofarm.seller.farm.domain.Farm;
import com.barofarm.seller.farm.domain.FarmImage;
import com.barofarm.seller.farm.domain.Status;
import java.util.UUID;

public record FarmCreateInfo(
    UUID id,
    String name,
    String description,
    String address,
    String phone,
    String email,
    Integer establishedYear,
    String farmSize,
    String cultivationMethod,
    Status status,
    UUID sellerId,
    Image image
) {

    public static FarmCreateInfo from(Farm farm) {
        return new FarmCreateInfo(
            farm.getId(),
            farm.getName(),
            farm.getDescription(),
            farm.getAddress(),
            farm.getPhone(),
            farm.getEmail(),
            farm.getEstablishedYear(),
            farm.getFarmSize(),
            farm.getCultivationMethod(),
            farm.getStatus(),
            farm.getSeller().getId(),
            farm.getImage() != null ? Image.from(farm.getImage()) : null
        );
    }

    public record Image(
        UUID id,
        String url,
        String s3Key
    ) {
        public static Image from(FarmImage image) {
            return new Image(
                image.getId(),
                image.getUrl(),
                image.getS3Key()
            );
        }
    }
}
