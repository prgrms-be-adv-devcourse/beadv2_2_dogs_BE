package com.barofarm.seller.seller.infrastructure;

import com.barofarm.seller.seller.domain.validation.SellerDuplicationChecker;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/*도메인 검증을 인프라(JPA)로부터 분리하는 포트 역할:
SimpleBusinessValidator는 “중복 여부”만 알고, 구현은 SellerDuplicationCheckerJpa로 위임합니다.
다른 저장소(캐시, 외부 서비스 등)로 바꿀 때 도메인 코드 수정이 필요 없습니다.*/
@Component
@RequiredArgsConstructor
public class SellerDuplicationCheckerJpa implements SellerDuplicationChecker {

    private final SellerJpaRepository sellerJpaRepository;

    @Override
    public boolean existsByUserId(UUID userId) {
        return sellerJpaRepository.existsById(userId);
    }

    @Override
    public boolean existsByBusinessRegNo(String businessRegNo) {
        return sellerJpaRepository.existsByBusinessRegNo(businessRegNo);
    }
}
