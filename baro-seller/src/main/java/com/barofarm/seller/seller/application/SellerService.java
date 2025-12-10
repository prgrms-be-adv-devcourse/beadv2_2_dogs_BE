package com.barofarm.seller.seller.application;

import com.barofarm.seller.common.exception.CustomException;
import com.barofarm.seller.seller.domain.Seller;
import com.barofarm.seller.seller.domain.validation.BusinessValidator;
import com.barofarm.seller.seller.exception.FeignErrorCode;
import com.barofarm.seller.seller.infrastructure.SellerJpaRepository;
import com.barofarm.seller.seller.infrastructure.feign.AuthClient;
import com.barofarm.seller.seller.presentation.dto.SellerApplyRequestDto;
import feign.FeignException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SellerService {

    private final SellerJpaRepository sellerJpaRepository;
    private final AuthClient authClient;

    // 지금은 simpleBusinessValidator 주입된 상태
    private final BusinessValidator businessValidator;

    public void applyForSeller(UUID userId, SellerApplyRequestDto requestDto) {

        // 1. 검증
        businessValidator.validate(requestDto.businessRegNo(), requestDto.businessOwnerName());

        // 2. Seller프로필 생성하고, APPROVED로 설정 (승인 절차 따로 없음 현재는)
        Seller profile = Seller.createApproved(
            userId,
            requestDto.storeName(),
            requestDto.businessRegNo(),
            requestDto.businessOwnerName(),
            requestDto.settlementBank(),
            requestDto.settlementAccount()
        );

        sellerJpaRepository.save(profile);

        // 3. auth-service에 SELLER 권한 부여 요청(Feign)
        try {
            authClient.grantSeller(userId);
        } catch (FeignException e) {
            // 타임아웃/5xx 계열 구분 가능하지만, 공통적으로 게이트웨이 오류로 맵핑
            throw new CustomException(FeignErrorCode.AUTH_SERVICE_ERROR);
        }


    }



}
