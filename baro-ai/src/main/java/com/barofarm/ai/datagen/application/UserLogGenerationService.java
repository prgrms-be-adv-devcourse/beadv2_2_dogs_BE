package com.barofarm.ai.datagen.application;

import com.barofarm.ai.log.domain.CartLogDocument;
import com.barofarm.ai.log.domain.OrderLogDocument;
import com.barofarm.ai.log.domain.SearchLogDocument;
import com.barofarm.ai.log.infrastructure.elasticsearch.CartLogRepository;
import com.barofarm.ai.log.infrastructure.elasticsearch.OrderLogRepository;
import com.barofarm.ai.log.infrastructure.elasticsearch.SearchLogRepository;
import com.barofarm.ai.search.application.UnifiedSearchService;
import com.barofarm.ai.search.domain.ProductDocument;
import com.barofarm.ai.search.infrastructure.elasticsearch.ProductSearchRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * 테스트 및 개발을 위한 사용자 행동 로그 생성 서비스
 * Kafka 이벤트가 아직 머지되지 않아 실제 이벤트를 받을 수 없을 때,
 * Elasticsearch에 직접 더미 로그를 생성하여 개인화 추천 시스템을 테스트할 수 있게 합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserLogGenerationService {

    private final CartLogRepository cartLogRepository;
    private final OrderLogRepository orderLogRepository;
    private final SearchLogRepository searchLogRepository;
    private final ChatClient chatClient;
    private final UnifiedSearchService unifiedSearchService;
    private final ProductSearchRepository productSearchRepository;
    private final Random random = new Random();

    // 하드코딩된 테스트용 사용자 ID
    private static final UUID TEST_USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    // 기본 기간 (최근 30일)
    private static final int DEFAULT_DAYS_BACK = 30;

    // UserProfileEmbeddingService가 사용하는 이벤트 타입들
    private static final String[] CART_EVENT_TYPES = {
        "CART_ITEM_ADDED",        // 가중치 2
        "CART_QUANTITY_UPDATED",  // 가중치 1
        "CART_ITEM_REMOVED"       // 가중치 0 (제외됨)
    };

    private static final String[] ORDER_EVENT_TYPES = {
        "ORDER_CONFIRMED",   // 가중치 3
        "ORDER_CANCELLED"    // 가중치 0 (제외됨)
    };

    /**
     * 하드코딩된 테스트 사용자에 대한 더미 행동 로그를 생성합니다.
     * UserProfileEmbeddingService가 사용하는 로그 형식에 맞춰 생성합니다.
     *
     * <p><b>동작 방식:</b>
     * <ol>
     *   <li>새 로그 생성:
     *       <ul>
     *         <li>검색 로그: LLM으로 생성된 키워드 사용 (통합 검색 실행)</li>
     *         <li>장바구니/주문 로그: Elasticsearch에서 랜덤으로 가져온 상품 사용</li>
     *       </ul>
     *   </li>
     *   <li>로그는 계속 쌓이며, UserProfileEmbeddingService는 최신순으로 각 타입별 최대 5개씩만 사용</li>
     * </ol>
     *
     * <p><b>주의사항:</b>
     * <ul>
     *   <li>로그 생성 후 반드시 프로필 임베딩을 재생성해야 추천 결과가 변경됩니다</li>
     *   <li>프로필 임베딩 재생성: POST /api/v1/datagen/user-profile-embedding</li>
     *   <li>기존 로그는 삭제되지 않으며, 최신순으로 5개씩만 사용됩니다</li>
     * </ul>
     */
    @SuppressWarnings("checkstyle:MethodLength")
    public void generateDummyLogsForUser() {
        UUID userId = TEST_USER_ID;
        int daysBack = DEFAULT_DAYS_BACK;

        // Elasticsearch에서 랜덤 상품 가져오기
        List<ProductDocument> randomProducts = getRandomProducts(10); // 충분한 수량 확보
        if (randomProducts.isEmpty()) {
            log.error("❌ Elasticsearch에서 상품을 가져올 수 없습니다. 상품이 인덱싱되어 있는지 확인하세요.");
            return;
        }

        Instant now = Instant.now();
        List<SearchLogDocument> searchLogs = new ArrayList<>();
        List<CartLogDocument> cartLogs = new ArrayList<>();
        List<OrderLogDocument> orderLogs = new ArrayList<>();

        // UserProfileEmbeddingService는 각 타입별로 최대 5개씩만 사용하므로 5개로 제한
        int maxLogsPerType = 5;

        // 1. 검색 로그 생성 (LLM으로 검색 키워드 생성 후 통합 검색 실행)
        try {
            List<String> searchKeywords = generateSearchKeywordsWithLLM();

            for (int i = 0; i < Math.min(maxLogsPerType, searchKeywords.size()); i++) {
                String searchQuery = searchKeywords.get(i);

                // 과거 daysBack일 범위 내에서 랜덤 시간 생성
                Instant searchedAt = now.minus(random.nextInt(daysBack), ChronoUnit.DAYS)
                    .minus(random.nextInt(24), ChronoUnit.HOURS)
                    .minus(random.nextInt(60), ChronoUnit.MINUTES);

                // 통합 검색 실행 (검색 로그는 ProductSearchService.searchProducts()에서 자동 저장됨)
                Pageable pageable = PageRequest.of(0, 10);
                try {
                    unifiedSearchService.search(userId, searchQuery, pageable);

                    // 통합 검색이 성공하면 자동으로 로그가 저장되지만, 시간이 현재 시간으로 저장됨
                    // 과거 시간으로 업데이트하기 위해 최근 저장된 로그를 찾아서 시간 수정
                    List<SearchLogDocument> recentLogs = searchLogRepository
                        .findAllByUserIdAndSearchedAtAfterOrderBySearchedAtDesc(
                            userId,
                            now.minus(1, ChronoUnit.MINUTES), // 최근 1분 내
                            PageRequest.of(0, 1)
                        );

                    if (!recentLogs.isEmpty()) {
                        SearchLogDocument recentLog = recentLogs.get(0);
                        // 검색어가 일치하고 시간이 최근인 경우 업데이트
                        if (recentLog.getSearchQuery().equals(searchQuery)) {
                            // 기존 로그 삭제 후 과거 시간으로 새로 저장
                            searchLogRepository.deleteById(recentLog.getId());

                            SearchLogDocument updatedLog = SearchLogDocument.builder()
                                .userId(recentLog.getUserId())
                                .searchQuery(recentLog.getSearchQuery())
                                .category(recentLog.getCategory())
                                .searchedAt(searchedAt) // 과거 시간으로 업데이트
                                .build();

                            SearchLogDocument saved = searchLogRepository.save(updatedLog);
                            searchLogs.add(saved);
                            log.debug("✅ 통합 검색 실행 및 로그 시간 업데이트: User={}, Query='{}', Time={}",
                                userId, searchQuery, searchedAt);
                        } else {
                            searchLogs.add(recentLog);
                            log.debug("✅ 통합 검색 실행 및 로그 저장: User={}, Query='{}'",
                                userId, searchQuery);
                        }
                    } else {
                        log.warn("⚠️ 통합 검색 후 저장된 로그를 찾을 수 없습니다: User={}, Query='{}'",
                            userId, searchQuery);
                    }
                } catch (Exception e) {
                    log.warn("⚠️ 통합 검색 실행 실패, 직접 로그 저장: User={}, Query='{}', Error={}",
                        userId, searchQuery, e.getMessage());

                    // 검색 실패 시 직접 로그 저장
                    SearchLogDocument searchLog = SearchLogDocument.builder()
                        .userId(userId)
                        .searchQuery(searchQuery)
                        .category(null)
                        .searchedAt(searchedAt)
                        .build();

                    SearchLogDocument saved = searchLogRepository.save(searchLog);
                    searchLogs.add(saved);
                }
            }
        } catch (Exception e) {
            log.error("❌ 검색 키워드 생성 실패: {}", e.getMessage(), e);
            // LLM 실패 시 기본 키워드 사용
            List<String> defaultKeywords = getDefaultSearchKeywords();
            for (int i = 0; i < Math.min(maxLogsPerType, defaultKeywords.size()); i++) {
                String searchQuery = defaultKeywords.get(i);
                Instant searchedAt = now.minus(random.nextInt(daysBack), ChronoUnit.DAYS)
                    .minus(random.nextInt(24), ChronoUnit.HOURS)
                    .minus(random.nextInt(60), ChronoUnit.MINUTES);

                SearchLogDocument searchLog = SearchLogDocument.builder()
                    .userId(userId)
                    .searchQuery(searchQuery)
                    .category(null)
                    .searchedAt(searchedAt)
                    .build();

                SearchLogDocument saved = searchLogRepository.save(searchLog);
                searchLogs.add(saved);
            }
        }

        // 2. 장바구니 로그 생성 (최대 5개) - 랜덤 상품 사용
        for (int i = 0; i < maxLogsPerType; i++) {
            ProductDocument randomProduct = randomProducts.get(random.nextInt(randomProducts.size()));
            UUID productId = randomProduct.getProductId();
            String productName = randomProduct.getProductName();
            // 가중치가 높은 이벤트를 더 많이 생성 (CART_ITEM_ADDED가 70% 확률)
            String eventType = random.nextDouble() < 0.7
                ? "CART_ITEM_ADDED"
                : CART_EVENT_TYPES[random.nextInt(CART_EVENT_TYPES.length)];

            int quantity = random.nextInt(5) + 1; // 1~5개
            Instant occurredAt = now.minus(random.nextInt(daysBack), ChronoUnit.DAYS)
                .minus(random.nextInt(24), ChronoUnit.HOURS)
                .minus(random.nextInt(60), ChronoUnit.MINUTES);

            CartLogDocument cartLog = CartLogDocument.builder()
                .userId(userId)
                .productId(productId) // Elasticsearch에서 가져온 실제 productId
                .productName(productName)
                .categoryName(product.getCategory()) // 가짜 로그용, 검색 인덱스의 category 사용
                .eventType(eventType)
                .quantity(quantity)
                .occurredAt(occurredAt)
                .build();

            CartLogDocument saved = cartLogRepository.save(cartLog);
            cartLogs.add(saved);
            log.debug("✅ 장바구니 로그 생성: User={}, Product='{}', Event={}, Qty={}, Time={}",
                userId, productName, eventType, quantity, occurredAt);
        }

        // 3. 주문 로그 생성 (최대 5개) - 랜덤 상품 사용
        for (int i = 0; i < maxLogsPerType; i++) {
            ProductDocument randomProduct = randomProducts.get(random.nextInt(randomProducts.size()));
            UUID productId = randomProduct.getProductId();
            String productName = randomProduct.getProductName();
            // ORDER_CONFIRMED가 80% 확률로 생성 (가중치가 높으므로)
            String eventType = random.nextDouble() < 0.8
                ? "ORDER_CONFIRMED"
                : ORDER_EVENT_TYPES[random.nextInt(ORDER_EVENT_TYPES.length)];

            int quantity = random.nextInt(3) + 1; // 1~3개
            Instant occurredAt = now.minus(random.nextInt(daysBack), ChronoUnit.DAYS)
                .minus(random.nextInt(24), ChronoUnit.HOURS)
                .minus(random.nextInt(60), ChronoUnit.MINUTES);

            OrderLogDocument orderLog = OrderLogDocument.builder()
                .userId(userId)
                .productId(productId) // Elasticsearch에서 가져온 실제 productId
                .productName(productName)
                .categoryName(product.getCategory()) // 검색 인덱스의 category를 그대로 사용
                .eventType(eventType)
                .quantity(quantity)
                .occurredAt(occurredAt)
                .build();

            OrderLogDocument saved = orderLogRepository.save(orderLog);
            orderLogs.add(saved);
            log.debug("✅ 주문 로그 생성: User={}, Product='{}', Event={}, Qty={}, Time={}",
                userId, productName, eventType, quantity, occurredAt);
        }

        log.info("✅ 더미 로그 생성 완료 - User: {}, 검색: {}개, 장바구니: {}개, 주문: {}개 (총 {}개)",
            userId, searchLogs.size(), cartLogs.size(), orderLogs.size(),
            searchLogs.size() + cartLogs.size() + orderLogs.size());
    }

    /**
     * LLM(OpenAI)을 사용하여 농산물/수산물/축산물 검색 시 사용자가 입력할 것 같은 검색 키워드를 생성합니다.
     *
     * <p><b>동작 방식:</b>
     * <ol>
     *   <li>OpenAI ChatClient를 사용하여 프롬프트 전송</li>
     *   <li>LLM이 JSON 배열 형식으로 검색 키워드 5개 생성</li>
     *   <li>ParameterizedTypeReference를 사용하여 자동 파싱</li>
     *   <li>실패 시 기본 키워드 반환 (fallback)</li>
     * </ol>
     *
     * <p><b>프롬프트 전략:</b>
     * <ul>
     *   <li>역할 설정: "프리미엄 신선식품 이커머스에서 쇼핑하는 사용자"</li>
     *   <li>명확한 요구사항: 자연스러운 키워드, 농산물/수산물/축산물 관련, 1~3단어</li>
     *   <li>예시 제공: 참고용 예시로 품질 향상</li>
     *   <li>출력 형식 명시: JSON 배열만 반환하도록 지시</li>
     * </ul>
     *
     * <p><b>에러 처리:</b>
     * <ul>
     *   <li>LLM API 호출 실패: 기본 키워드 사용</li>
     *   <li>빈 결과 반환: 기본 키워드 사용</li>
     *   <li>유효하지 않은 키워드: 필터링 후 기본 키워드 사용</li>
     * </ul>
     *
     * <p><b>로그 레벨:</b>
     * <ul>
     *   <li>INFO: LLM 호출 시작/완료 (성공 시)</li>
     *   <li>WARN: LLM 실패 또는 빈 결과 (기본 키워드 사용)</li>
     *   <li>ERROR: 예외 발생 (기본 키워드 사용)</li>
     * </ul>
     *
     * @return 검색 키워드 리스트 (최대 5개, LLM 실패 시 기본 키워드 반환)
     */
    private List<String> generateSearchKeywordsWithLLM() {
        String promptTemplate = """
            당신은 프리미엄 신선식품 이커머스에서 쇼핑하는 사용자입니다.
            농산물, 수산물, 축산물을 검색할 때 실제 사용자가 입력할 것 같은 검색 키워드를 생성해주세요.

            [요구사항]
            1. 실제 사용자가 검색창에 입력할 것 같은 자연스러운 키워드
            2. 농산물/수산물/축산물 관련 키워드
            3. 상품명이 아닌 검색어 형태 (예: "사과", "제주 감귤", "한우", "연어" 등)
                - 꼭 예시와 동일할 필요는 없습니다.
            4. 각 키워드는 1~3단어로 구성
            5. 총 5개의 서로 다른 키워드 생성

            [예시(참고용)]
            - "사과"
            - "제주 감귤"
            - "1등급 한우"
            - "노르웨이산 연어"
            - "청송 사과"
            - "바나나"
            - "고등어"
            - "닭가슴살"

            반드시 JSON 배열 형식으로만 반환하세요. 추가 설명이나 마크다운은 포함하지 마세요.
            ["키워드1", "키워드2", "키워드3", "키워드4", "키워드5"]
            """;

        try {
            log.info("🔄 [LLM] OpenAI를 사용하여 검색 키워드 생성 시작...");

            List<String> keywords = chatClient.prompt()
                .user(p -> p.text(promptTemplate))
                .call()
                .entity(new ParameterizedTypeReference<List<String>>() {});

            if (keywords == null || keywords.isEmpty()) {
                log.warn("⚠️ [LLM] OpenAI가 빈 키워드 리스트를 반환했습니다. 기본 키워드 사용");
                return getDefaultSearchKeywords();
            }

            // 최대 5개로 제한 및 유효성 검사
            List<String> result = keywords.stream()
                .limit(5)
                .filter(keyword -> keyword != null && !keyword.trim().isEmpty())
                .toList();

            if (result.isEmpty()) {
                log.warn("⚠️ [LLM] 유효한 키워드가 없습니다. 기본 키워드 사용");
                return getDefaultSearchKeywords();
            }

            log.info("✅ [LLM] OpenAI 검색 키워드 생성 완료: {} (총 {}개)", result, result.size());
            return result;

        } catch (Exception e) {
            log.error("❌ [LLM] OpenAI 검색 키워드 생성 실패: {} (기본 키워드 사용)", e.getMessage(), e);
            return getDefaultSearchKeywords();
        }
    }

    /**
     * LLM 실패 시 사용할 기본 검색 키워드
     *
     * <p>OpenAI API 호출이 실패하거나 빈 결과를 반환할 때 사용하는 fallback 키워드입니다.
     *
     * @return 기본 검색 키워드 리스트 (5개)
     */
    private List<String> getDefaultSearchKeywords() {
        log.info("📝 [FALLBACK] 기본 검색 키워드 사용: [사과, 바나나, 한우, 연어, 닭가슴살]");
        return List.of(
            "사과",
            "바나나",
            "한우",
            "연어",
            "닭가슴살"
        );
    }

    /**
     * Elasticsearch에서 랜덤 상품을 가져옵니다.
     *
     * @param count 가져올 상품 개수
     * @return 랜덤 상품 리스트
     */
    private List<ProductDocument> getRandomProducts(int count) {
        try {
            // Elasticsearch에서 모든 상품 가져오기 (상태가 ON_SALE 또는 DISCOUNTED인 것만)
            List<ProductDocument> allProducts = new ArrayList<>();
            Pageable pageable = PageRequest.of(0, 1000); // 충분한 수량

            Iterable<ProductDocument> products = productSearchRepository.findAll();
            for (ProductDocument product : products) {
                // ON_SALE 또는 DISCOUNTED 상태만 필터링
                if (product.getStatus() != null &&
                    (product.getStatus().equals("ON_SALE") || product.getStatus().equals("DISCOUNTED"))) {
                    allProducts.add(product);
                }
            }

            if (allProducts.isEmpty()) {
                log.warn("⚠️ Elasticsearch에 유효한 상품이 없습니다.");
                return List.of();
            }

            // 랜덤하게 섞기
            Collections.shuffle(allProducts, random);

            // 요청한 개수만큼 반환
            int actualCount = Math.min(count, allProducts.size());
            return allProducts.subList(0, actualCount);

        } catch (Exception e) {
            log.error("❌ Elasticsearch에서 상품을 가져오는 중 오류 발생: {}", e.getMessage(), e);
            return List.of();
        }
    }
}
