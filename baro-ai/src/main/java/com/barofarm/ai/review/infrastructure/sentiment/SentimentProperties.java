package com.barofarm.ai.review.infrastructure.sentiment;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "sentiment")
public class SentimentProperties {

    private Rating rating = new Rating();
    private Thresholds thresholds = new Thresholds();

    private List<String> positive = List.of();
    private List<String> negative = List.of();
    private List<String> transition = List.of();

    @Getter
    @Setter
    public static class Rating {
        private List<Integer> positive = List.of(4, 5);
        private List<Integer> mixed = List.of(3);
        private List<Integer> negative = List.of(1, 2);
    }

    @Getter
    @Setter
    public static class Thresholds {
        private int tolerance = 2;   // 반대 키워드 허용치
        private int strongDiff = 3;  // pos-neg 차이로 뒤집는 기준
        private int mixedMin = 4;    // 반대 키워드가 이 정도 이상이면 MIXED
    }
}
