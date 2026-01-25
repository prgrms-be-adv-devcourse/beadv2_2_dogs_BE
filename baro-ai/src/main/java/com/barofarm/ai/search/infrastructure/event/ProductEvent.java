package com.barofarm.ai.search.infrastructure.event;

import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ProductEvent - baro-buyer 모듈에서 발행하는 이벤트와 동일한 구조
 * MSA 환경에서 모듈 간 직접 의존성을 피하기 위해 각 모듈에 동일한 구조의 클래스를 유지
 * JSON 역직렬화를 위해 기본 생성자와 setter 사용
 */
@Getter
@Setter
@NoArgsConstructor
public class ProductEvent {

    private ProductEventType type;
    private ProductEventData data;

    public enum ProductEventType {
        PRODUCT_CREATED,
        PRODUCT_UPDATED,
        PRODUCT_DELETED
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ProductEventData {
        private UUID productId;
        private String productName;
        private UUID productCategoryId;
        private String productCategoryCode;
        private String productCategoryName;
        private Long price;
        private String status;
        private Instant updatedAt;
    }
}
