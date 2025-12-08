package com.barofarm.support.experience.infrastructure;

import com.barofarm.support.experience.domain.Experience;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** 체험 프로그램 JPA Repository */
public interface ExperienceJpaRepository extends JpaRepository<Experience, Long> {

    /**
     * 농장 ID로 체험 프로그램 목록 조회
     *
     * @param farmId
     *            농장 ID
     * @return 체험 프로그램 목록
     */
    List<Experience> findByFarmId(Long farmId);

    /**
     * 농장 ID와 체험 ID로 존재 여부 확인
     *
     * @param farmId
     *            농장 ID
     * @param id
     *            체험 ID
     * @return 존재 여부
     */
    boolean existsByFarmIdAndId(Long farmId, Long id);
}
