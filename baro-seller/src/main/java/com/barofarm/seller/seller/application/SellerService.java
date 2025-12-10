package com.barofarm.seller.seller.application;

import com.barofarm.seller.common.exception.CustomException;
import com.barofarm.seller.seller.domain.Seller;
import com.barofarm.seller.seller.domain.validation.BusinessValidator;
import com.barofarm.seller.seller.exception.SellerErrorCode;
import com.barofarm.seller.seller.infrastructure.SellerJpaRepository;
import com.barofarm.seller.seller.infrastructure.feign.AuthClient;
import com.barofarm.seller.seller.presentation.dto.SellerApplyRequestDto;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SellerService {

    private final SellerJpaRepository sellerJpaRepository;
    private final AuthClient authClient;

    // 현재는 SimpleBusinessValidator만 주입해서 사용
    private final BusinessValidator businessValidator;

    public void applyForSeller(UUID userId, SellerApplyRequestDto requestDto) {

        // 1. 사업자 등록번호/대표자 검증
        businessValidator.validate(requestDto.businessRegNo(), requestDto.businessOwnerName());
        ensureNotDuplicateSeller(userId);

        // 2. 셀러 프로필 생성 및 APPROVED 상태 설정(간이 승인 절차 기준)
        Seller profile = Seller.createApproved(
            userId,
            requestDto.storeName(),
            requestDto.businessRegNo(),
            requestDto.businessOwnerName(),
            requestDto.settlementBank(),
            requestDto.settlementAccount()
        );

        sellerJpaRepository.save(profile);

        // 커밋이 실패했는데, feign으로 users테이블만 변경되는걸 막음
        // 3. auth-service에 SELLER 권한 부여 요청(Feign) - 커밋 이후에만 실행
        runAfterCommit(() -> {
            log.info("[SELLER] grantSeller Feign 호출 시작, userId={}", userId);
            authClient.grantSeller(userId);
            log.info("[SELLER] grantSeller Feign 호출 성공, userId={}", userId);
        });
    }

    // 이미 판매자로 등록된 userId라면 중복 등록을 막는다
    private void ensureNotDuplicateSeller(UUID userId) {
        if (sellerJpaRepository.existsById(userId)) {
            throw new CustomException(SellerErrorCode.SELLER_ALREADY_EXISTS);
        }
    }

    // 트랜잭션 커밋 이후에만 실행되도록 등록하고, 트랜잭션이 없으면 즉시 실행한다
    private void runAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
            return;
        }
        action.run();
    }
}
