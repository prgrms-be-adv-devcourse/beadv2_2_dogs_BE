package com.barofarm.ai.review.domain.review;

public enum Sentiment {
    POSITIVE("P"),
    NEGATIVE("N"),
    MIXED("M");

    private final String code;

    Sentiment(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
