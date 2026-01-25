package com.barofarm.ai.recommend.application.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// SpringBoot 설정 프로퍼티를 자바 객체로 바인딩
@Component
@ConfigurationProperties(prefix = "baro.ai.recommend")
@Getter
@Setter
public class RecommendProperties {

    // 레시피 추천 시 제외할 상품 카테고리 코드 목록
    private List<String> excludeRecipeCategoryCodes = new ArrayList<>();
}
