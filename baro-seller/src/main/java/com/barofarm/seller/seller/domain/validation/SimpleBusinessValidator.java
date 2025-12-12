package com.barofarm.seller.seller.domain.validation;

import static com.barofarm.seller.seller.exception.ValidationErrorCode.DUPLICATE_BUSINESS_NO;
import static com.barofarm.seller.seller.exception.ValidationErrorCode.INVALID_BUSINESS_NO_FORMAT;
import static com.barofarm.seller.seller.exception.ValidationErrorCode.REQUIRED_FIELD_MISSING;

import com.barofarm.seller.common.exception.CustomException;
import com.barofarm.seller.seller.exception.SellerErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 사업자 등록번호/대표자명 검증 + 중복 검증(포트 사용).
 * DB 의존은 SellerDuplicationChecker 포트를 통해 위임한다.
 */
@Component
@RequiredArgsConstructor
public class SimpleBusinessValidator implements BusinessValidator {

    private final SellerDuplicationChecker duplicationChecker;

    @Override
    public void validate(UUID userId, String businessRegNo, String businessOwnerName) {

        // 1. 필수 값 확인
        if (!StringUtils.hasText(businessRegNo) || !StringUtils.hasText(businessOwnerName)) {
            throw new CustomException(REQUIRED_FIELD_MISSING);
        }

        // 2. 사업자번호 길이 검증(기본 10자리)
        if (businessRegNo.length() != 10) {
            throw new CustomException(INVALID_BUSINESS_NO_FORMAT);
        }

        // 3. 사업자번호 중복 검증 (DB 의존 → 포트 사용)
        if (duplicationChecker.existsByBusinessRegNo(businessRegNo)) {
            throw new CustomException(DUPLICATE_BUSINESS_NO);
        }

        // 4. 유저 기준 중복 셀러 검증 (DB 의존 → 포트 사용)
        if (duplicationChecker.existsByUserId(userId)) {
            throw new CustomException(SellerErrorCode.SELLER_ALREADY_EXISTS);
        }
    }
}
