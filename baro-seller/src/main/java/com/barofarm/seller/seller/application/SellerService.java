package com.barofarm.seller.seller.application;

import static com.barofarm.seller.seller.exception.SellerErrorCode.SELLER_NOT_FOUND;

import com.barofarm.seller.common.exception.CustomException;
import com.barofarm.seller.seller.domain.Seller;
import com.barofarm.seller.seller.domain.SellerRepository;
import com.barofarm.seller.seller.domain.validation.BusinessValidator;
import com.barofarm.seller.seller.exception.FeignErrorCode;
import com.barofarm.seller.seller.exception.ValidationErrorCode;
import com.barofarm.seller.seller.infrastructure.feign.AuthClient;
import com.barofarm.seller.seller.presentation.dto.SellerApplyRequestDto;
import com.barofarm.seller.seller.presentation.dto.SellerInfoDto;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerRepository sellerRepository;
    private final AuthClient authClient;

    // 현재는 SimpleBusinessValidator만 주입해서 사용
    private final BusinessValidator businessValidator;

    // 프론트에서 요청한 UserId로 조회하는 Seller 정보
    @Transactional(readOnly = true)
    public SellerInfoDto getASellerByUserId(UUID userId) {

        Seller seller = sellerRepository.findById(userId)
            .orElseThrow(() -> new CustomException(SELLER_NOT_FOUND));

        return SellerInfoDto.from(seller);
    }

    // bulk형태로 여러개 들어오면 한번에 여러개 Seller정보 주기
    @Transactional(readOnly = true)
    public List<SellerInfoDto> getSellersByIds(List<UUID> userIds) {

        if(userIds == null || userIds.isEmpty()){
            throw new CustomException(ValidationErrorCode.REQUIRED_FIELD_MISSING);
        }

        // 받을 수 있는 길이 100개로 제한
        if(userIds.size() > 100){
            throw new CustomException(ValidationErrorCode.REQUIRED_TOO_LARGE);
        }

        // 중복처리 방지 하기
        List<UUID> distinctIds = userIds.stream().distinct().toList();
        List<Seller> sellers = sellerRepository.findByIdIn(distinctIds);

        Set<UUID> foundIds = sellers.stream().map(Seller::getId).collect(Collectors.toSet());
        List<UUID> missing = distinctIds.stream()
            .filter(id -> !foundIds.contains(id))
            .toList();

        // TODO: MISSING인 것들 보여주기 지금은 그냥 NOTFOUND만 나옴
        if (!missing.isEmpty()) {
            throw new CustomException(SELLER_NOT_FOUND);
        }

        return sellers.stream()
            .map(SellerInfoDto::from)
            .toList();
    }


    @Transactional
    public void applyForSeller(UUID userId, SellerApplyRequestDto requestDto) {

        // 1. 사업자 등록번호/대표자 검증
        businessValidator.validate(userId, requestDto.businessRegNo(), requestDto.businessOwnerName());

        // 2. 셀러 프로필 생성 및 APPROVED 상태 설정(간이 승인 절차 기준)
        Seller profile = Seller.createApproved(
            userId,
            requestDto.storeName(),
            requestDto.businessRegNo(),
            requestDto.businessOwnerName(),
            requestDto.settlementBank(),
            requestDto.settlementAccount()
        );

        sellerRepository.save(profile);

        // 커밋이 실패했는데, feign으로 users테이블만 변경되는걸 막음
        // 3. auth-service에 SELLER 권한 부여 요청(Feign) - 커밋 이후에만 실행
        //    - 커밋 이후 롤백이 불가하므로 Feign 실패 시 3회 재시도 + 로그 후 CustomException 변환
        runAfterCommit(() -> callGrantSellerWithRetry(userId));
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

    // 트랜잭션 커밋 이후 Auth 서비스에 권한 부여를 요청하며, 네트워크 단절 등을 대비해 간단한 재시도와 로그를 남긴다.
    private void callGrantSellerWithRetry(UUID userId) {
        final int maxAttempts = 3;               // 최대 3회 재시도
        final long baseBackoffMillis = 500L;     // 재시도 간 기본 대기 시간(증가형)

        log.info("[SELLER] grantSeller Feign 호출 시작, userId={}", userId);
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                authClient.grantSeller(userId);  // 실제 Feign 호출
                log.info("[SELLER] grantSeller Feign 호출 성공(attempt={}), userId={}", attempt, userId);
                return;
            } catch (Exception ex) {
                // 실패마다 warn 로그로 남기고, 마지막 시도까지 실패하면 error 로그 후 CustomException 변환
                log.warn("[SELLER] grantSeller Feign 호출 실패(attempt={}): {}", attempt, ex.getMessage(), ex);
                if (attempt == maxAttempts) {
                    log.error("[SELLER] grantSeller Feign 최종 실패 - CustomException 발생, userId={}", userId);
                    throw new CustomException(FeignErrorCode.AUTH_SERVICE_UNAVAILABLE);
                }
                try {
                    Thread.sleep(baseBackoffMillis * attempt); // 점진적 백오프: auth 죽었을까봐 기다리는 거
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    log.warn("[SELLER] grantSeller 재시도 대기 중 인터럽트 발생, 즉시 중단 userId={}", userId);
                    throw new CustomException(FeignErrorCode.AUTH_SERVICE_UNAVAILABLE);
                }
            }
        }
    }
}
