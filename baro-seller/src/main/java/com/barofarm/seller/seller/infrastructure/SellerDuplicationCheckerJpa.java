package com.barofarm.seller.seller.infrastructure;

import com.barofarm.seller.seller.domain.validation.SellerDuplicationChecker;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
