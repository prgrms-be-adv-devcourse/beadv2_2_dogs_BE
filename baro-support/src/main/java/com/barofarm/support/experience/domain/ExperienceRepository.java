package com.barofarm.support.experience.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** 체험 프로그램 리포지토리 인터페이스 (Port) */
public interface ExperienceRepository {

    /**
     * 체험 프로그램 저장
     *
     * @param experience 저장할 체험 프로그램
     * @return 저장된 체험 프로그램
     */
    Experience save(Experience experience);

    /**
     * ID로 체험 프로그램 조회
     *
     * @param experienceId 체험 ID
     * @return 체험 프로그램 (Optional)
     */
    Optional<Experience> findById(UUID experienceId);

    /**
     * 농장 ID로 체험 프로그램 목록 조회
     *
     * @param farmId 농장 ID
     * @return 체험 프로그램 목록
     */
    List<Experience> findByFarmId(UUID farmId);

    /**
     * 모든 체험 프로그램 조회
     *
     * @return 모든 체험 프로그램 목록
     */
    List<Experience> findAll();

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
     * ID로 체험 프로그램 존재 여부 확인
     *
     * @param experienceId 체험 ID
     * @return 존재 여부
     */
    boolean existsById(UUID experienceId);

    /**
     * 체험 프로그램 삭제
     *
     * @param experienceId 체험 ID
     */
    void deleteById(UUID experienceId);

    /**
     * 농장 ID와 체험 ID로 존재 여부 확인
     *
     * @param farmId 농장 ID
     * @param experienceId 체험 ID
     * @return 존재 여부
     */
    boolean existsByFarmIdAndExperienceId(UUID farmId, UUID experienceId);
}
