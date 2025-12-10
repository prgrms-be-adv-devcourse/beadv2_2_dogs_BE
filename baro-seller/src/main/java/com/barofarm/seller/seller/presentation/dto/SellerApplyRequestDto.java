package com.barofarm.seller.seller.presentation.dto;

public record SellerApplyRequestDto(
    String storeName,
    String businessRegNo,
    String businessOwnerName,
    String settlementBank,
    String settlementAccount
) {
}
