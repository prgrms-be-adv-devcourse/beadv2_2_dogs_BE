package com.barofarm.ai.season.application;

import com.barofarm.ai.season.infrastructure.client.ProductQueryFeignClient;
import com.barofarm.ai.season.infrastructure.client.dto.ProductSummary;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 제철 정보 판단을 위한 주기적 스케줄러
 * Spring Batch 없이 가벼운 @Scheduled 방식 사용
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SeasonalityScheduler {

    private final SeasonalityDetectionService detectionService;
    private final ProductQueryFeignClient productQueryClient;

    /**
     * 30분마다 실행
     * 제철 정보가 없는 기존 상품들의 제철을 판단
     * 한 번에 최대 10개만 처리
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void detectSeasonalityForExistingProducts() {
        log.info("기존 상품 제철 판단 주기 작업 시작");

        try {
            // 한 번에 10개만 처리 (처리량 제한)
            int pageSize = 10;
            int page = 0;

            List<ProductSummary> products = productQueryClient.getProductsWithoutSeasonality(page, pageSize);

            if (products.isEmpty()) {
                log.info("제철 정보가 없는 상품이 없습니다.");
                return;
            }

            log.info("제철 정보가 없는 상품 {}개 발견, 제철 판단 시작", products.size());

            // 각 상품에 대해 비동기로 제철 판단 수행
            for (ProductSummary product : products) {
                try {
                    detectionService.detectSeasonalityAsync(
                        product.id(),
                        product.productName(),
                        product.categoryCode()
                    );
                    // Rate Limit 방지를 위해 약간의 대기
                    Thread.sleep(500);
                } catch (Exception e) {
                    log.error("상품 제철 판단 실패: productId={}, productName={}",
                        product.id(), product.productName(), e);
                }
            }

            log.info("기존 상품 제철 판단 주기 작업 완료: 처리된 상품 수 = {}", products.size());

        } catch (Exception e) {
            log.error("기존 상품 제철 판단 주기 작업 실패", e);
        }
    }

    /**
     * 주 1회 실행 (일요일 새벽 3시)
     * 제철 정보가 있는 상품의 재검증 (선택사항)
     */
    @Scheduled(cron = "0 0 3 ? * SUN")
    public void revalidateSeasonality() {
        log.info("제철 정보 재검증 주기 작업 시작");
        // TODO: 구현 필요 (선택사항)
    }
}
