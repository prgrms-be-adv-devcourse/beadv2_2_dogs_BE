package com.barofarm.support.common.response;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public record CustomPage<T>(

    List<T> content,

    int page,

    int size,

    long totalElements,

    int totalPages,

    boolean first,

    boolean last,

    boolean hasNext,

    boolean hasPrevious
) {
    public static <T> CustomPage<T> from(Page<T> page) {
        return new CustomPage<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast(),
            page.hasNext(),
            page.hasPrevious()
        );
    }

    /**
     * List와 total, Pageable을 받아서 CustomPage 생성
     * (ES 검색 결과 등 Page<T>가 아닌 경우 사용)
     */
    public static <T> CustomPage<T> of(long total, List<T> content, Pageable pageable) {
        int page = Math.max(pageable.getPageNumber(), 0);
        int size = pageable.getPageSize();
        int totalPages = size <= 0 ? 0 : (int) Math.ceil((double) total / size);
        boolean hasNext = page + 1 < totalPages;
        boolean hasPrevious = page > 0;
        boolean isFirst = page == 0;
        boolean isLast = totalPages == 0 || !hasNext;

        return new CustomPage<>(
            content,
            page,
            size,
            total,
            totalPages,
            isFirst,
            isLast,
            hasNext,
            hasPrevious
        );
    }
}
