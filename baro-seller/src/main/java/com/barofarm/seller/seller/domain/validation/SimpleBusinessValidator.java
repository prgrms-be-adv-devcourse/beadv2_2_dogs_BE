package com.barofarm.seller.seller.domain.validation;

import static com.barofarm.seller.seller.exception.ValidationErrorCode.DUPLICATE_BUSINESS_NO;
import static com.barofarm.seller.seller.exception.ValidationErrorCode.INVALID_BUSINESS_NO_FORMAT;
import static com.barofarm.seller.seller.exception.ValidationErrorCode.REQUIRED_FIELD_MISSING;

import com.barofarm.seller.common.exception.CustomException;
import com.barofarm.seller.seller.infrastructure.SellerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

// 실제 국세청 API 연동 전 -> 형식이랑 중복 정도만 검증
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SimpleBusinessValidator implements BusinessValidator {
    private final SellerJpaRepository sellerJpaRepository;

    @Override
    public void validate(String businessRegNo, String businessOwnerName) {

        // 1. 빈 값 체크
        if (!StringUtils.hasText(businessRegNo) || !StringUtils.hasText(businessOwnerName)) {
            throw new CustomException(REQUIRED_FIELD_MISSING);
        }

        // 2. 형식(길이) 간단 검증 - 예시로 10자 검증
        if (businessRegNo.length() != 10) {
            throw new CustomException(INVALID_BUSINESS_NO_FORMAT);
        }

        // 3. 이미 등록된건지 중복 체크
        if (sellerJpaRepository.existsByBusinessRegNo(businessRegNo)) {
            throw new CustomException(DUPLICATE_BUSINESS_NO);
        }


    }

}
