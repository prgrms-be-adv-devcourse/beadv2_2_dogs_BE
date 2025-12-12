package com.barofarm.seller.seller.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SellerApplyRequestDto(
    @NotBlank @Size(max = 50) String storeName,
    @NotBlank @Pattern(regexp = "\\d{10}") String businessRegNo,
    @NotBlank @Size(max = 30) String businessOwnerName,
    @NotBlank @Size(max = 30) String settlementBank,
    @NotBlank @Size(max = 30) String settlementAccount
) {
}
