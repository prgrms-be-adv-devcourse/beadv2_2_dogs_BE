package com.barofarm.seller.seller.domain.validation;

import java.util.UUID;

/**
 * 도메인에서 중복 여부를 확인하기 위한 포트.
 * 실제 구현은 인프라 레이어(JPA 등)에서 담당한다.
 */
public interface SellerDuplicationChecker {

    boolean existsByUserId(UUID userId);

    boolean existsByBusinessRegNo(String businessRegNo);
}
