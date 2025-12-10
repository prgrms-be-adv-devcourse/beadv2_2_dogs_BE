package com.barofarm.seller.seller.domain.validation;

public interface BusinessValidator {
    /**
     * 사업자 정보 검증 :
     *
     * @param businessRegNo       사업자등록번호
     * @param businessOwnerName    대표자명
     * @throws com.barofarm.seller.seller.exception.ValidationErrorCode 실패 시
     */
    void validate(String businessRegNo, String businessOwnerName);
}
