package com.barofarm.support.review.domain;

public enum ReviewStatus {
    PUBLIC,      //공개
    PRIVATE,     //비공개
    HIDDEN,      //관리자 숨김
    DELETED;     //삭제

    public static final ReviewStatus DEFAULT = PUBLIC;

    public static ReviewStatus defaultStatus() {
        return DEFAULT;
    }
}
