package com.barofarm.support.review.domain;

import com.barofarm.support.review.presentation.dto.ReviewVisibility;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ReviewStatus {
    PUBLIC(true),      //공개
    PRIVATE(true),     //비공개
    HIDDEN(false),     //관리자 숨김
    DELETED(false);    //삭제

    public static final ReviewStatus DEFAULT = PUBLIC;
    private static final EnumSet<ReviewStatus> VISIBLE_TO_OWNER = EnumSet.of(PUBLIC, PRIVATE);
    private static final EnumSet<ReviewStatus> VISIBLE_TO_PUBLIC = EnumSet.of(PUBLIC);

    private final boolean userEditable;

    public boolean isNotUserEditable() {
        return !userEditable;
    }

    public boolean isVisibleToOwner() {
        return VISIBLE_TO_OWNER.contains(this);
    }

    public boolean isVisibleToPublic() {
        return VISIBLE_TO_PUBLIC.contains(this);
    }

    public static Set<ReviewStatus> getVisibleToOwnerSet() {
        return Collections.unmodifiableSet(VISIBLE_TO_OWNER);
    }

    public static Set<ReviewStatus> getVisibleToPublicSet() {
        return Collections.unmodifiableSet(VISIBLE_TO_PUBLIC);
    }

    public static ReviewStatus fromVisibility(ReviewVisibility visibility) {
        return switch (visibility) {
            case PUBLIC -> PUBLIC;
            case PRIVATE -> PRIVATE;
        };
    }
}
