package com.barofarm.seller.farm.application.dto.response;

import com.barofarm.seller.farm.domain.Farm;
import com.barofarm.seller.farm.domain.FarmImage;
import com.barofarm.seller.farm.domain.Status;
import java.util.UUID;

public record FarmListInfo(
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
    ImageDto image
) {
    public static FarmListInfo from(Farm farm) {
        return new FarmListInfo(
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
            farm.getImage() == null ? null : ImageDto.from(farm.getImage())
        );
    }

    public record ImageDto(
        UUID id,
        String url,
        String s3Key
    ) {
        public static ImageDto from(FarmImage image) {
            return new ImageDto(image.getId(), image.getUrl(), image.getS3Key());
        }
    }
}
