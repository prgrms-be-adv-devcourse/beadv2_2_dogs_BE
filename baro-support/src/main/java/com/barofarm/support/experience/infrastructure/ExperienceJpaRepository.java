package com.barofarm.support.experience.infrastructure;

import com.barofarm.support.experience.domain.Experience;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** 체험 프로그램 JPA Repository */
public interface ExperienceJpaRepository extends JpaRepository<Experience, UUID> {

    /**
     * 농장 ID로 체험 프로그램 목록 조회
     *
     * @param farmId 농장 ID
     * @return 체험 프로그램 목록
     */
    List<Experience> findByFarmId(UUID farmId);

    /**
     * 농장 ID로 체험 프로그램 목록 조회 (페이지네이션)
     *
     * @param farmId 농장 ID
     * @param pageable 페이지 정보
     * @return 체험 프로그램 페이지
     */
    Page<Experience> findByFarmId(UUID farmId, Pageable pageable);

    /**
     * 모든 체험 프로그램 조회 (페이지네이션)
     *
     * @param pageable 페이지 정보
     * @return 체험 프로그램 페이지
     */
    Page<Experience> findAll(Pageable pageable);

    /**
     * 농장 ID와 체험 ID로 존재 여부 확인
     *
     * @param farmId 농장 ID
     * @param experienceId 체험 ID
     * @return 존재 여부
     */
    boolean existsByFarmIdAndExperienceId(UUID farmId, UUID experienceId);
}
