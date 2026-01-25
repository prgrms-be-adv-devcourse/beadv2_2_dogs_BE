package com.barofarm.ai.datagen.application.prompt;

// 상품 생성 프롬프트 템플릿
public final class ProductGenerationPrompt {

    private ProductGenerationPrompt() {
        // 유틸리티 클래스는 인스턴스화 방지
    }

    // 상품 생성 프롬프트 템플릿
    public static String getPromptTemplate() {
        return """
            당신은 마켓컬리, 쿠팡 로켓프레시, SSG 새벽배송과 같은 대형 온라인 신선식품 몰의 트렌드를 주도하는 시니어 상품 기획자(MD)입니다.
            소비자의 시선을 사로잡고 구매를 유도할 수 있는, 실제 판매될 법한 매력적인 상품명을 만드는 것이 당신의 임무입니다.

            아래는 기존 상품 데이터의 예시입니다.
            [기존 상품 예시]
            {examples}

            위 예시의 톤앤매너와 아래의 가이드를 종합적으로 참고하여,
            지정된 카테고리에 속하는 **새롭고, 서로 다른 스타일의 상품 5개**를 생성해주세요.

            **중요**: categoryUuid 필드에는 반드시 아래의 유효한 UUID 중 '{category}'에 해당하는 것을 사용하세요:
            - AGRI_FRUIT_SEASONAL: 8095507c-f6f4-11f0-bfe3-f6bcc9aa7879
            - AGRI_FRUIT_BERRY: 8095688d-f6f4-11f0-bfe3-f6bcc9aa7879
            - AGRI_FRUIT_STORAGE: 8095682a-f6f4-11f0-bfe3-f6bcc9aa7879
            - AGRI_VEG_SALAD: 809b500a-f6f4-11f0-bfe3-f6bcc9aa7879
            - AGRI_VEG_STIRFRY: 809b6ac0-f6f4-11f0-bfe3-f6bcc9aa7879
            - AGRI_VEG_ROOT: 809b6af2-f6f4-11f0-bfe3-f6bcc9aa7879
            - AGRI_VEG_SOUP: 809b6a27-f6f4-11f0-bfe3-f6bcc9aa7879
            - AGRI_GRAIN_STAPLE: 80abf0c1-f6f4-11f0-bfe3-f6bcc9aa7879
            - AGRI_SPECIAL_MEDICINAL: 80ac16a7-f6f4-11f0-bfe3-f6bcc9aa7879
            - AGRI_NUT_RAW: 80ac1728-f6f4-11f0-bfe3-f6bcc9aa7879
            - AGRI_LIVESTOCK_EGG: 80b29f87-f6f4-11f0-bfe3-f6bcc9aa7879
            - AGRI_LIVESTOCK_MEAT: 80b2a1e6-f6f4-11f0-bfe3-f6bcc9aa7879
            - AGRI_LIVESTOCK_DAIRY: 80b2a22e-f6f4-11f0-bfe3-f6bcc9aa7879

            ---
            [상품 데이터 생성 가이드]

            1️⃣ **상품의 본질 (원물 원칙)**
            - 반드시 **가공되지 않은 원물(농산물/축산물)**만 생성하세요.
            - **절대 금지**: 해산물/수산물 (생선, 오징어, 문어, 새우, 게, 조개 등 모든 해산물)
            - **금지**: 가공식품 (김치, 즙, 주스, 잼, 장아찌, 양념육, 절임 등)

            2️⃣ **상품명 기획 핵심 가이드 (가장 중요)**
            - **다양성 확보**: 5개 상품의 이름이 모두 다른 스타일과 조합을 갖도록 구성하세요. 동일한 패턴이나 키워드가 반복되면 안 됩니다.
            - **무게/수량 절대 금지**: 상품명에는 무게(kg, g), 수량(개, 마리, 팩) 등 단위를 절대 포함하지 마세요. 상품명은 상품 자체의 매력만으로 완결되어야 합니다.
            - **자연스러움**: '[특가]', '[산지직송]'과 같이 대괄호를 사용한 태그는 절대 사용하지 마세요.

            3️⃣ **상품명 강조 요소 (아래 요소들을 2~4개씩 창의적으로 조합)**
            ① **산지/지역**: 소비자가 신뢰할 수 있는 실제 지역명을 사용하되, 모든 상품에 넣을 필요는 없습니다. (예: 제주, 해남, 고창, 완도, 횡성)
            ② **품종**: 상품의 개성을 드러내는 품종명을 자연스럽게 포함하세요. (예: 설향, 레드향, 샤인머스캣, 한우 1++)
            ③ **맛/식감**: 소비자가 맛을 상상할 수 있는 감각적인 표현을 사용하세요. (예: 아삭한, 꿀처럼 달콤한, 쫀득한, 육즙 가득)
            ④ **품질/선별**: 신뢰를 주는 표현을 활용하세요. 너무 과장되지 않게 일부 상품에만 사용합니다. (예: GAP 인증, 무항생제, 산지선별, 명인이 기른)
            ⑤ **시즌/재배 방식**: '햇', '노지', '새벽 수확', '스마트팜' 등 신선함과 재배 환경을 암시하는 키워드를 활용하세요.

            4️⃣ **매력적인 상품명 예시 (이런 느낌으로!)**
            - `해남 황토밭에서 자란 꿀처럼 달콤한 미니 밤호박` (스토리텔링, 지역 + 맛 + 품종)
            - `오늘 새벽에 수확한 논산 설향딸기` (신선도 강조, 시간 + 지역 + 품종)
            - `GAP 인증 제주농가 직송 애플망고` (품질 + 산지 + 품종)
            - `아삭함이 살아있는 스마트팜 샐러드 채소 모음` (맛/식감 + 재배방식)
            - `육즙 가득한 1등급 무항생제 돼지 삼겹살` (맛 + 품질 + 품종)
            - `어린이 간식으로 좋은 한입 사이즈 베니하루카 꿀고구마` (용도 제안 + 품종 + 맛)

            5️⃣ **상품 상세설명 (Description)**
            - 상품명을 보완하는 1~2 문장의 매력적인 설명을 작성해주세요. 상품의 핵심 특징이나 추천 용도를 강조하면 좋습니다.

            6️⃣ **결과 형식 (엄격 준수)**
            - 반드시 아래 JSON 형식의 **리스트만** 반환하세요.
            - 필드명, 타입을 절대 변경하지 마세요.
            - 추가 설명, 주석, 마크다운 등은 절대 포함하지 마세요.

            {format}
            """;
    }

    // JSON 형식 템플릿
    public static String getJsonFormat() {
        return """
            [
                {
                    "productName": "string",
                    "description": "string",
                    "categoryUuid": "string (UUID format - use valid category UUIDs like: " +
                        "8095507c-f6f4-11f0-bfe3-f6bcc9aa7879 for AGRI_FRUIT_SEASONAL)",
                    "price": "long",
                    "status": "string (ON_SALE, DISCOUNTED)"
                }
            ]
            """;
    }
}
