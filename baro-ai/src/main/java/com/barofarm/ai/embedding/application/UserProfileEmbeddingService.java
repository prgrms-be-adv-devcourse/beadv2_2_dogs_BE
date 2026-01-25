package com.barofarm.ai.embedding.application;

import com.barofarm.ai.embedding.domain.UserProfileEmbeddingDocument;
import com.barofarm.ai.embedding.exception.EmbeddingErrorCode;
import com.barofarm.ai.embedding.infrastructure.elasticsearch.UserProfileEmbeddingRepository;
import com.barofarm.ai.log.domain.CartLogDocument;
import com.barofarm.ai.log.domain.OrderLogDocument;
import com.barofarm.ai.log.domain.SearchLogDocument;
import com.barofarm.ai.log.infrastructure.elasticsearch.CartLogRepository;
import com.barofarm.ai.log.infrastructure.elasticsearch.OrderLogRepository;
import com.barofarm.ai.log.infrastructure.elasticsearch.SearchLogRepository;
import com.barofarm.exception.CustomException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class UserProfileEmbeddingService {

    // 프로필 벡터 임베딩을 위한 최소 로그 개수
    private static final int MIN_TOTAL_LOGS_FOR_EMBEDDING = 3;

    private final EmbeddingModel embeddingModel;
    private final CartLogRepository cartLogRepository;
    private final OrderLogRepository orderLogRepository;
    private final SearchLogRepository searchLogRepository;
    private final UserProfileEmbeddingRepository userProfileEmbeddingRepository;

    public UserProfileEmbeddingService(
        @Qualifier("openAiEmbeddingModel") EmbeddingModel embeddingModel,
        CartLogRepository cartLogRepository,
        OrderLogRepository orderLogRepository,
        SearchLogRepository searchLogRepository,
        UserProfileEmbeddingRepository userProfileEmbeddingRepository
    ) {
        this.embeddingModel = embeddingModel;
        this.cartLogRepository = cartLogRepository;
        this.orderLogRepository = orderLogRepository;
        this.searchLogRepository = searchLogRepository;
        this.userProfileEmbeddingRepository = userProfileEmbeddingRepository;
    }

    // 특정 사용자의 활동 로그를 가져와서 프로필 벡터를 생성/업데이트
    public void updateUserProfileEmbedding(UUID userId) {
        // 1. 최근 30일간의 사용자 활동 로그를 각 타입별로 최대 5개씩 가져옵니다.
        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        Pageable top5 = PageRequest.of(0, 5);

        List<SearchLogDocument> searchLogs = searchLogRepository
            .findAllByUserIdAndSearchedAtAfterOrderBySearchedAtDesc(userId, thirtyDaysAgo, top5);
        List<CartLogDocument> cartLogs = cartLogRepository
            .findAllByUserIdAndOccurredAtAfterOrderByOccurredAtDesc(userId, thirtyDaysAgo, top5);
        List<OrderLogDocument> orderLogs = orderLogRepository
            .findAllByUserIdAndOccurredAtAfterOrderByOccurredAtDesc(userId, thirtyDaysAgo, top5);

        // 1.5. 임베딩 생성에 사용한 로그들의 ID 수집 (동일성/재현성 확보)
        List<String> sourceSearchLogIds = searchLogs.stream()
            .map(SearchLogDocument::getId)
            .collect(Collectors.toList());

        List<String> sourceCartLogIds = cartLogs.stream()
            .map(CartLogDocument::getId)
            .collect(Collectors.toList());

        List<String> sourceOrderLogIds = orderLogs.stream()
            .map(OrderLogDocument::getId)
            .collect(Collectors.toList());

        // productId 추출 (cart + order에서만, UUID를 문자열로 변환)
        List<String> sourceProductIds = Stream.concat(
                cartLogs.stream().map(cart -> cart.getProductId().toString()),
                orderLogs.stream().map(order -> order.getProductId().toString())
            )
            .distinct()  // 중복 제거
            .collect(Collectors.toList());

        int totalLogCount = searchLogs.size() + cartLogs.size() + orderLogs.size();
        int minTotalLogs = MIN_TOTAL_LOGS_FOR_EMBEDDING;

        // 로그 개수가 최소 개수 이하인 경우
        if (totalLogCount < minTotalLogs) {
            log.info("사용자 ID {}의 총 로그 개수({})가 {}개 미만이므로 임베딩을 건너뜁니다. " +
                    "(검색:{}, 장바구니:{}, 주문:{})",
                    userId, totalLogCount, minTotalLogs,
                    searchLogs.size(), cartLogs.size(), orderLogs.size());
            return;
        }

        log.debug("사용자 ID {}의 로그 데이터: 검색={}, 장바구니={}, 주문={}",
                 userId, searchLogs.size(), cartLogs.size(), orderLogs.size());

        // 2. 로그를 바탕으로 임베딩할 대표 텍스트 생성
        String representativeText = buildRepresentativeText(searchLogs, cartLogs, orderLogs);

        if (!StringUtils.hasText(representativeText)) {
            log.info("사용자 ID {}에 대한 임베딩을 생성할 충분한 텍스트 데이터가 없습니다.", userId);
            return;
        }

        // 3. 대표 텍스트를 임베딩하여 벡터 생성
        List<Double> vector;
        try {
            vector = embedText(representativeText);
        } catch (Exception e) {
            log.error("사용자 ID {}의 임베딩 생성 실패: {}", userId, e.getMessage(), e);
            return;
        }

        // 3.5. 선호 카테고리 코드 계산 (cart/order 로그에서 가중치 적용하여 최빈값 도출)
        String preferredCategoryCode = calculatePreferredCategoryCode(cartLogs, orderLogs);

        // 4. 생성된 벡터로 UserProfileEmbeddingDocument를 만들어 저장
        UserProfileEmbeddingDocument embeddingDocument = UserProfileEmbeddingDocument.builder()
            .userId(userId)
            .userProfileVector(vector)
            .lastUpdatedAt(Instant.now())
            // 임베딩 생성에 사용한 로그들의 ID (동일성/재현성 확보)
            .sourceSearchLogIds(sourceSearchLogIds)
            .sourceCartLogIds(sourceCartLogIds)
            .sourceOrderLogIds(sourceOrderLogIds)
            // 빠른 상품 제외를 위한 productId 목록
            .sourceProductIds(sourceProductIds)
            // 사용자 선호 카테고리 코드
            .preferredCategoryCode(preferredCategoryCode)
            .build();

        userProfileEmbeddingRepository.save(embeddingDocument);

        log.info("사용자 ID {}의 프로필 벡터를 성공적으로 업데이트했습니다. (총 {}개 로그 사용)",
                userId, totalLogCount);
    }

    // 벡터로 임베딩할 대표 텍스트 만들기
    private String buildRepresentativeText(
        List<SearchLogDocument> searchLogs,
        List<CartLogDocument> cartLogs,
        List<OrderLogDocument> orderLogs
    ) {
        // 1. 검색어 텍스트 (시간 가중치 적용)
        Stream<String> searchKeywords = searchLogs.stream()
            .filter(log -> StringUtils.hasText(log.getSearchQuery()))
            .flatMap(this::expandSearchQueryWithTimeWeight);

        // 2. 장바구니 상품 텍스트 (수량 + 이벤트 + 시간 가중치 적용)
        Stream<String> cartProductTexts = cartLogs.stream()
            .filter(log -> StringUtils.hasText(log.getProductName()))
            .flatMap(this::expandCartProductWithFullWeight);

        // 3. 주문 상품 텍스트 (수량 + 이벤트 + 시간 가중치 적용)
        Stream<String> orderProductTexts = orderLogs.stream()
            .filter(log -> StringUtils.hasText(log.getProductName()))
            .flatMap(this::expandOrderProductWithFullWeight);

        // 모든 텍스트를 결합
        return Stream.of(searchKeywords, cartProductTexts, orderProductTexts)
            .flatMap(s -> s)
            .collect(Collectors.joining(", "));
    }

    // Search 로그에 시간 가중치 적용
    private Stream<String> expandSearchQueryWithTimeWeight(SearchLogDocument log) {
        double timeWeight = calculateTimeWeight(log.getSearchedAt());
        long finalWeight = Math.max(1L, Math.round(timeWeight * 1.0)); // 검색어 기본 가중치 1.0

        return Stream.generate(() -> log.getSearchQuery())
            .limit(finalWeight);
    }

    // Cart 로그에 수량, 이벤트 타입, 시간 가중치 모두 적용
    private Stream<String> expandCartProductWithFullWeight(CartLogDocument log) {
        int eventWeight = calculateCartEventWeight(log.getEventType());
        int quantityWeight = Math.max(1, log.getQuantity());
        double timeWeight = calculateTimeWeight(log.getOccurredAt());

        long totalWeight = Math.max(1L, Math.round((double) eventWeight * quantityWeight * timeWeight));

        return Stream.generate(() -> log.getProductName())
            .limit(totalWeight);
    }

    // Order 로그에 수량, 이벤트 타입, 시간 가중치 모두 적용
    private Stream<String> expandOrderProductWithFullWeight(OrderLogDocument log) {
        int eventWeight = calculateOrderEventWeight(log.getEventType());
        int quantityWeight = Math.max(1, log.getQuantity());
        double timeWeight = calculateTimeWeight(log.getOccurredAt());

        long totalWeight = Math.max(1L, Math.round((double) eventWeight * quantityWeight * timeWeight));

        return Stream.generate(() -> log.getProductName())
            .limit(totalWeight);
    }

    // 장바구니 이벤트 타입별 가중치 계산
    private int calculateCartEventWeight(String eventType) {
        return switch (eventType) {
            case "CART_ITEM_ADDED" -> 2;      // 상품 추가: 관심 표현
            case "CART_QUANTITY_UPDATED" -> 1;   // 수량 변경: 관심 유지
            case "CART_ITEM_REMOVED" -> 0;   // 상품 제거: 관심 감소 (가중치 제거)
            default -> 1;
        };
    }

    // 주문 이벤트 타입별 가중치 계산
    private int calculateOrderEventWeight(String eventType) {
        return switch (eventType) {
            case "ORDER_CONFIRMED" -> 3;   // 주문 완료: 가장 높은 관심
            case "ORDER_CANCELLED" -> 0; // 주문 취소: 관심 제거
            default -> 1;
        };
    }

    // 이벤트 발생 시간에 따른 가중치를 계산 (최근 이벤트일수록 높은 가중치 부여)
    private double calculateTimeWeight(Instant eventTime) {
        long hoursAgo = ChronoUnit.HOURS.between(eventTime, Instant.now());

        // 지수 감쇠: 시간이 지날수록 가중치 감소
        // 7일(168시간) 기준으로 0.5배까지 감쇠
        double decayFactor = Math.exp(-hoursAgo / 168.0);

        // 최소 가중치 0.3, 최대 가중치 3.0
        double timeWeight = Math.max(0.3, 3.0 * decayFactor);

        return Math.min(timeWeight, 3.0); // 최대 3배 제한
    }

    // cart/order 로그에서 선호 카테고리 코드 계산 (이벤트 가중치 적용)
    // 사용자의 장바구니 및 주문 로그를 분석하여 가장 선호하는 카테고리 1개를 도출합니다.
    private String calculatePreferredCategoryCode(
        List<CartLogDocument> cartLogs,
        List<OrderLogDocument> orderLogs
    ) {
        // 카테고리별 가중치 합계를 계산
        Map<String, Double> categoryWeights = new java.util.HashMap<>();

        // Cart 로그 처리
        for (CartLogDocument cartLog : cartLogs) {
            String categoryCode = cartLog.getCategoryCode();
            if (categoryCode == null) {
                continue;
            }

            int eventWeight = calculateCartEventWeight(cartLog.getEventType());
            int quantityWeight = Math.max(1, cartLog.getQuantity());
            double timeWeight = calculateTimeWeight(cartLog.getOccurredAt());

            double totalWeight = eventWeight * quantityWeight * timeWeight;
            categoryWeights.merge(categoryCode, totalWeight, Double::sum);
        }

        // Order 로그 처리
        for (OrderLogDocument orderLog : orderLogs) {
            String categoryCode = orderLog.getCategoryCode();
            if (categoryCode == null) {
                continue;
            }

            int eventWeight = calculateOrderEventWeight(orderLog.getEventType());
            int quantityWeight = Math.max(1, orderLog.getQuantity());
            double timeWeight = calculateTimeWeight(orderLog.getOccurredAt());

            double totalWeight = eventWeight * quantityWeight * timeWeight;
            categoryWeights.merge(categoryCode, totalWeight, Double::sum);
        }

        // 가중치가 가장 큰 카테고리 코드 선택
        return categoryWeights.entrySet().stream()
            .max(Comparator.comparingDouble(Map.Entry::getValue))
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    // 텍스트를 임베딩하여 벡터로 변환
    private List<Double> embedText(String text) {
        try {
            log.debug("🔄 [USER_PROFILE_EMBEDDING] Generating embedding for text length: {}", text.length());

            // EmbeddingModel은 List<String>을 받아서 List<float[]>를 반환
            var embeddings = embeddingModel.embed(List.of(text));

            if (embeddings.isEmpty()) {
                log.warn("⚠️ [USER_PROFILE_EMBEDDING] No embedding generated for text");
                throw new RuntimeException("Failed to generate embedding: empty result");
            }

            // float[]를 List<Double>로 변환
            float[] floatVector = embeddings.get(0);
            List<Double> doubleVector = new ArrayList<>(floatVector.length);
            for (float f : floatVector) {
                doubleVector.add((double) f);
            }

            log.debug("✅ [USER_PROFILE_EMBEDDING] Successfully generated embedding with {} dimensions",
                    doubleVector.size());
            return doubleVector;

        } catch (Exception e) {
            log.error("❌ [USER_PROFILE_EMBEDDING] Failed to generate embedding: {}", e.getMessage(), e);
            throw new CustomException(EmbeddingErrorCode.EMBEDDING_GENERATION_FAILED);
        }
    }
}
