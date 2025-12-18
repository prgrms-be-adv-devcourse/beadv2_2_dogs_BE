package com.barofarm.seller.seller.presentation.dto;

import com.barofarm.seller.seller.domain.Seller;
import com.barofarm.seller.seller.domain.Status;

public record SellerInfoDto(
    String storeName,
    String businessRegNo,
    String businessOwnerName,
    Status status
) {
    // Seller -> SellerInfoDto로 변환하는 메서드
    public static SellerInfoDto from(Seller seller) {
        return new SellerInfoDto(
            seller.getStoreName(),
            seller.getBusinessRegNo(),
            seller.getBusinessOwnerName(),
            seller.getStatus()
        );
    }
}
