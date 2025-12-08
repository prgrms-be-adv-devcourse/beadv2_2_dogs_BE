package com.barofarm.support.experience.domain;

import java.util.List;
import java.util.Optional;

/** 체험 프로그램 리포지토리 인터페이스 (Port) */
public interface ExperienceRepository {

    /**
     * 체험 프로그램 저장
     *
     * @param experience
     *            저장할 체험 프로그램
     * @return 저장된 체험 프로그램
     */
    Experience save(Experience experience);

    /**
     * ID로 체험 프로그램 조회
     *
     * @param id
     *            체험 ID
     * @return 체험 프로그램 (Optional)
     */
    Optional<Experience> findById(Long id);

    /**
     * 농장 ID로 체험 프로그램 목록 조회
     *
     * @param farmId
     *            농장 ID
     * @return 체험 프로그램 목록
     */
    List<Experience> findByFarmId(Long farmId);

    /**
     * 모든 체험 프로그램 조회
     *
     * @return 모든 체험 프로그램 목록
     */
    List<Experience> findAll();

    /**
     * ID로 체험 프로그램 존재 여부 확인
     *
     * @param id
     *            체험 ID
     * @return 존재 여부
     */
    boolean existsById(Long id);

    /**
     * 체험 프로그램 삭제
     *
     * @param id
     *            체험 ID
     */
    void deleteById(Long id);

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
