package com.barofarm.ai.datagen.application.constants;

import java.util.Map;

// 데이터 생성 관련 상수 정의
public final class DataGenConstants {

    private DataGenConstants() {
        // 유틸리티 클래스는 인스턴스화 방지
    }

    // 유효한 상품 카테고리 목록 (Level 2 카테고리 코드)
    public static final String[] VALID_CATEGORIES = {
        "AGRI_FRUIT_SEASONAL", "AGRI_FRUIT_STORAGE", "AGRI_FRUIT_BERRY",
        "AGRI_VEG_SALAD", "AGRI_VEG_SOUP", "AGRI_VEG_STIRFRY", "AGRI_VEG_ROOT",
        "AGRI_GRAIN_STAPLE", "AGRI_SPECIAL_MEDICINAL", "AGRI_NUT_RAW",
        "AGRI_LOCAL_REPRESENT", "AGRI_LIVESTOCK_EGG", "AGRI_LIVESTOCK_MEAT", "AGRI_LIVESTOCK_DAIRY"
    };

    // 카테고리 UUID와 코드 매핑 (product_dummy_origin.sql에 사용된 UUID들)
    public static final Map<String, String> CATEGORY_UUID_TO_CODE = Map.ofEntries(
        Map.entry("8095507c-f6f4-11f0-bfe3-f6bcc9aa7879", "AGRI_FRUIT_SEASONAL"),  // 과일류
        Map.entry("8095688d-f6f4-11f0-bfe3-f6bcc9aa7879", "AGRI_FRUIT_BERRY"),     // 베리류
        Map.entry("8095682a-f6f4-11f0-bfe3-f6bcc9aa7879", "AGRI_FRUIT_STORAGE"),   // 저장 과일
        Map.entry("809b500a-f6f4-11f0-bfe3-f6bcc9aa7879", "AGRI_VEG_SALAD"),       // 쌈·샐러드 채소
        Map.entry("809b6ac0-f6f4-11f0-bfe3-f6bcc9aa7879", "AGRI_VEG_STIRFRY"),     // 볶음·구이 채소
        Map.entry("809b6af2-f6f4-11f0-bfe3-f6bcc9aa7879", "AGRI_VEG_ROOT"),        // 뿌리채소
        Map.entry("809b6a27-f6f4-11f0-bfe3-f6bcc9aa7879", "AGRI_VEG_SOUP"),        // 국·찌개 채소
        Map.entry("80abf0c1-f6f4-11f0-bfe3-f6bcc9aa7879", "AGRI_GRAIN_STAPLE"),    // 식량작물
        Map.entry("80ac16a7-f6f4-11f0-bfe3-f6bcc9aa7879", "AGRI_SPECIAL_MEDICINAL"), // 특용작물
        Map.entry("80ac1728-f6f4-11f0-bfe3-f6bcc9aa7879", "AGRI_NUT_RAW"),         // 견과류
        Map.entry("80b29f87-f6f4-11f0-bfe3-f6bcc9aa7879", "AGRI_LIVESTOCK_EGG"),   // 계란
        Map.entry("80b2a1e6-f6f4-11f0-bfe3-f6bcc9aa7879", "AGRI_LIVESTOCK_MEAT"),  // 육류
        Map.entry("80b2a22e-f6f4-11f0-bfe3-f6bcc9aa7879", "AGRI_LIVESTOCK_DAIRY")  // 유제품
    );

    // 카테고리 코드와 UUID 역매핑
    public static final Map<String, String> CATEGORY_CODE_TO_UUID = Map.ofEntries(
        Map.entry("AGRI_FRUIT_SEASONAL", "8095507c-f6f4-11f0-bfe3-f6bcc9aa7879"),  // 과일류
        Map.entry("AGRI_FRUIT_BERRY", "8095688d-f6f4-11f0-bfe3-f6bcc9aa7879"),     // 베리류
        Map.entry("AGRI_FRUIT_STORAGE", "8095682a-f6f4-11f0-bfe3-f6bcc9aa7879"),   // 저장 과일
        Map.entry("AGRI_VEG_SALAD", "809b500a-f6f4-11f0-bfe3-f6bcc9aa7879"),       // 쌈·샐러드 채소
        Map.entry("AGRI_VEG_STIRFRY", "809b6ac0-f6f4-11f0-bfe3-f6bcc9aa7879"),     // 볶음·구이 채소
        Map.entry("AGRI_VEG_ROOT", "809b6af2-f6f4-11f0-bfe3-f6bcc9aa7879"),        // 뿌리채소
        Map.entry("AGRI_VEG_SOUP", "809b6a27-f6f4-11f0-bfe3-f6bcc9aa7879"),        // 국·찌개 채소
        Map.entry("AGRI_GRAIN_STAPLE", "80abf0c1-f6f4-11f0-bfe3-f6bcc9aa7879"),    // 식량작물
        Map.entry("AGRI_SPECIAL_MEDICINAL", "80ac16a7-f6f4-11f0-bfe3-f6bcc9aa7879"), // 특용작물
        Map.entry("AGRI_NUT_RAW", "80ac1728-f6f4-11f0-bfe3-f6bcc9aa7879"),         // 견과류
        Map.entry("AGRI_LIVESTOCK_EGG", "80b29f87-f6f4-11f0-bfe3-f6bcc9aa7879"),   // 계란
        Map.entry("AGRI_LIVESTOCK_MEAT", "80b2a1e6-f6f4-11f0-bfe3-f6bcc9aa7879"),  // 육류
        Map.entry("AGRI_LIVESTOCK_DAIRY", "80b2a22e-f6f4-11f0-bfe3-f6bcc9aa7879")  // 유제품
    );

    // 상품 생성 관련 상수
    public static final class ProductGeneration {
        private ProductGeneration() {
        }

        public static final int TARGET_COUNT = 1000;
        public static final int MAX_ITERATIONS = 1000;
        public static final int MAX_CONSECUTIVE_FAILURES = 10;
        public static final String DEFAULT_STATUS = "ON_SALE";
        public static final int MIN_PRICE = 100;
        public static final int MAX_PRICE = 1_000_000;
        public static final int MAX_PRODUCT_NAME_LENGTH = 100;
        public static final int MAX_DESCRIPTION_LENGTH = 500;
        public static final int MAX_EXAMPLES_COUNT = 3;
    }

    // 사용자 로그 생성 관련 상수
    public static final class UserLogGeneration {
        private UserLogGeneration() {
        }

        public static final String TEST_USER_ID = "550e8400-e29b-41d4-a716-446655440000";
        public static final int DEFAULT_DAYS_BACK = 30;
        public static final int MAX_LOGS_PER_TYPE = 5;
        public static final int RANDOM_PRODUCTS_COUNT = 10;
        public static final int MAX_PRODUCTS_FETCH_COUNT = 1000;
        public static final int MAX_SEARCH_KEYWORDS = 5;
        public static final double CART_ITEM_ADDED_PROBABILITY = 0.7;
        public static final double ORDER_CONFIRMED_PROBABILITY = 0.8;
        public static final int MIN_QUANTITY = 1;
        public static final int MAX_CART_QUANTITY = 5;
        public static final int MAX_ORDER_QUANTITY = 3;
    }

    // 파일 경로 관련 상수
    public static final class FilePaths {
        private FilePaths() {
        }

        public static final String DEFAULT_SQL_FILE_PATH = "scripts/generate-dummy/product_dummy_origin.sql";
        public static final String OUTPUT_DIR = "scripts/generate-dummy";
        public static final String OUTPUT_CSV_FILE = "amplified_product_data.csv";
    }

    // CSV 관련 상수
    public static final class Csv {
        private Csv() {
        }

        public static final String CSV_HEADER = "productName,description,categoryId,price,status";
        public static final String CSV_DELIMITER = ",";
        public static final String CSV_QUOTE = "\"";
        public static final String CSV_ESCAPED_QUOTE = "\"\"";
    }

    // 부적절한 콘텐츠 키워드 (가공식품, 해산물 등)
    public static final String[] INAPPROPRIATE_KEYWORDS = {
        "김치", "젓갈", "장아찌", "음료", "주스", "차", "커피", "술",
        // 해산물/수산물 금지
        "생선", "오징어", "문어", "새우", "게", "조개", "전복", "멍게", "해삼", "굴", "홍합", "바지락",
        "고등어", "삼치", "꽁치", "멸치", "참치", "연어", "광어", "우럭", "도미", "붕어", "잉어",
        "장어", "미꾸라지", "가재", "랍스터", "대게", "킹크랩", "꽃게", "방게", "꼬막", "소라",
        "바다", "해산", "수산", "어류", "어패류", "갑각류", "연체동물"
    };

    // 장바구니 이벤트 타입
    public static final String[] CART_EVENT_TYPES = {
        "CART_ITEM_ADDED",
        "CART_QUANTITY_UPDATED",
        "CART_ITEM_REMOVED"
    };

    // 주문 이벤트 타입
    public static final String[] ORDER_EVENT_TYPES = {
        "ORDER_CONFIRMED",
        "ORDER_CANCELLED"
    };
}
