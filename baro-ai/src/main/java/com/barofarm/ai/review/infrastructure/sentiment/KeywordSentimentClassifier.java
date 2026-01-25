package com.barofarm.ai.review.infrastructure.sentiment;

import com.barofarm.ai.review.application.sentiment.SentimentClassifier;
import com.barofarm.ai.review.domain.review.Sentiment;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class KeywordSentimentClassifier implements SentimentClassifier {

    private final SentimentProperties props;

    @Override
    public Sentiment classify(Integer rating, String content) {
        String normalized = normalize(content);
        Signals signals = extractSignals(normalized);
        Sentiment base = baseByRating(rating);

        return decide(base, signals);
    }

    ////// 1. 정규화 //////

    /**
     * 최소 정규화:
     * - trim, 소문자(영문 대응)
     * - 특수문자 -> 공백
     * - 연속 공백 정리
     */
    private String normalize(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        String s = content.trim().toLowerCase(Locale.ROOT); // 언어 중립 로케일

        // 너무 공격적인 정규화는 피하고, 매칭 방해되는 것만 최소 처리
        s = s.replaceAll("[^0-9a-z가-힣\\s]", " ");

        // 다중 공백 정리
        s = s.replaceAll("\\s+", " ");
        return s;
    }

    ////// 2. 긍정Hit/부정Hit/전환어 계산 //////

    /**
     * 텍스트에서 필요한 신호만 뽑아낸다.
     */
    private Signals extractSignals(String normalized) {
        int posHits = countHits(normalized, props.getPositive());
        int negHits = countHits(normalized, props.getNegative());
        boolean hasTransition = containsAny(normalized, props.getTransition());
        return new Signals(posHits, negHits, hasTransition);
    }

    /**
     * 매칭 개수 찾기
     */
    private int countHits(String normalized, List<String> keywords) {
        if (!StringUtils.hasText(normalized) || keywords == null || keywords.isEmpty()) {
            return 0;
        }

        int hits = 0;
        for (String raw : keywords) {
            if (!StringUtils.hasText(raw)) {
                continue;
            }
            String k = raw.trim();
            if (k.isEmpty()) {
                continue;
            }

            if (normalized.contains(k)) {
                hits++;
            }
        }
        return hits;
    }

    /**
     * 전환어 포함하는지 찾기
     */
    private boolean containsAny(String normalized, List<String> keywords) {
        if (!StringUtils.hasText(normalized) || keywords == null || keywords.isEmpty()) {
            return false;
        }

        for (String raw : keywords) {
            if (!StringUtils.hasText(raw)) {
                continue;
            }
            String k = raw.trim();
            if (!k.isEmpty() && normalized.contains(k)) {
                return true;
            }
        }
        return false;
    }

    /**
     * pos/neg/전환어 신호 묶음
     */
    private record Signals(int posHits, int negHits, boolean hasTransition) {
        int diff() {
            return posHits - negHits;
        }
        boolean hasBoth() {
            return posHits > 0 && negHits > 0;
        }
        boolean noSignal() {
            return posHits == 0 && negHits == 0;
        }
    }

    ////// 3. 별점으로 기본적인 감정 분류 //////

    /**
     * pos/neg/전환어 신호 묶음
     */
    private Sentiment baseByRating(Integer rating) {
        if (rating == null) {
            return Sentiment.MIXED;
        }

        List<Integer> pos = props.getRating().getPositive();
        List<Integer> neg = props.getRating().getNegative();
        List<Integer> mix = props.getRating().getMixed();

        if (pos != null && pos.contains(rating)) {
            return Sentiment.POSITIVE;
        }
        if (neg != null && neg.contains(rating)) {
            return Sentiment.NEGATIVE;
        }
        if (mix != null && mix.contains(rating)) {
            return Sentiment.MIXED;
        }

        return Sentiment.MIXED;
    }

    ////// 4. 최종 감정 분류 //////

    /**
     * 최종 감정 결정 로직
     * - strongDiff로 확 뒤집기
     * - 전환어 + 혼합이면 MIXED 가중
     * - 별점 base 유지 + tolerance 허용
     * - base가 MIXED면 텍스트로 결정
     */
    private Sentiment decide(Sentiment base, Signals s) {
        // 1) 매우 강한 텍스트 신호가 있으면 텍스트 우선
        int strongDiff = props.getThresholds().getStrongDiff();
        if (s.diff() >= strongDiff) {
            return Sentiment.POSITIVE;
        }
        if (-s.diff() >= strongDiff) {
            return Sentiment.NEGATIVE;
        }

        // 2) 전환어가 있고 긍/부정이 같이 있으면 MIXED 가중
        if (s.hasTransition() && s.hasBoth()) {
            return Sentiment.MIXED;
        }

        // 3) 별점 기반 base 유지 + 반대 키워드 허용치(tolerance)
        int tolerance = props.getThresholds().getTolerance();
        int mixedMin = props.getThresholds().getMixedMin();

        if (base == Sentiment.POSITIVE) {
            if (s.negHits() <= tolerance) {
                return Sentiment.POSITIVE;
            }
            if (s.negHits() >= mixedMin) {
                return Sentiment.MIXED;
            }
            return (s.posHits() >= s.negHits()) ? Sentiment.POSITIVE : Sentiment.MIXED;
        }

        if (base == Sentiment.NEGATIVE) {
            if (s.posHits() <= tolerance) {
                return Sentiment.NEGATIVE;
            }
            if (s.posHits() >= mixedMin) {
                return Sentiment.MIXED;
            }
            return (s.negHits() >= s.posHits()) ? Sentiment.NEGATIVE : Sentiment.MIXED;
        }

        // 4) base가 MIXED(=3점 또는 rating null 등)인 경우 텍스트로 결정
        if (s.noSignal()) {
            return Sentiment.MIXED;  // 정보 부족
        }
        if (s.hasBoth()) {
            return Sentiment.MIXED;   // 혼합 신호
        }
        return (s.posHits() >= s.negHits()) ? Sentiment.POSITIVE : Sentiment.NEGATIVE;
    }
}
