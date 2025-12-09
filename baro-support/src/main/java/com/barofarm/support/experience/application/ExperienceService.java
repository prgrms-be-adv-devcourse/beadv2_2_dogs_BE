package com.barofarm.support.experience.application;

import com.barofarm.support.common.exception.CustomException;
import com.barofarm.support.experience.application.dto.ExperienceServiceRequest;
import com.barofarm.support.experience.application.dto.ExperienceServiceResponse;
import com.barofarm.support.experience.domain.Experience;
import com.barofarm.support.experience.domain.ExperienceRepository;
import com.barofarm.support.experience.exception.ExperienceErrorCode;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 체험 프로그램 애플리케이션 서비스 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ExperienceService {

    private final ExperienceRepository experienceRepository;

    /**
     * 체험 프로그램 생성
     *
     * @param request
     *            체험 프로그램 생성 요청
     * @return 생성된 체험 프로그램
     */
    @Transactional
    public ExperienceServiceResponse createExperience(ExperienceServiceRequest request) {
        // TODO: access_token에서 user_type을 검사하여 SELLER인지 확인
        // - Gateway에서 전달된 X-User-Role 헤더를 확인
        // - SELLER가 아니면 예외 발생 (예: FARM_FORBIDDEN)

        Experience experience = request.toEntity();
        validateExperience(experience);
        Experience savedExperience = experienceRepository.save(experience);
        return ExperienceServiceResponse.from(savedExperience);
    }

    /**
     * ID로 체험 프로그램 조회
     *
     * @param experienceId 체험 ID
     * @return 체험 프로그램
     */
    public ExperienceServiceResponse getExperienceById(UUID experienceId) {
        // 디버깅용 로그
        System.out.println("Looking for experience with ID: " + experienceId);
        Experience experience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> {
                    System.out.println("Experience not found with ID: " + experienceId);
                    return new CustomException(ExperienceErrorCode.EXPERIENCE_NOT_FOUND);
                });
        System.out.println("Found experience: " + experience.getTitle());
        return ExperienceServiceResponse.from(experience);
    }

    /**
     * 농장 ID로 체험 프로그램 목록 조회
     *
     * @param farmId 농장 ID
     * @return 체험 프로그램 목록
     */
    public List<ExperienceServiceResponse> getExperiencesByFarmId(UUID farmId) {
        List<Experience> experiences = experienceRepository.findByFarmId(farmId);
        return experiences.stream().map(ExperienceServiceResponse::from).collect(Collectors.toList());
    }

    /**
     * 농장 ID로 체험 프로그램 목록 조회 (페이지네이션)
     *
     * @param farmId 농장 ID
     * @param pageable 페이지 정보
     * @return 체험 프로그램 페이지
     */
    public Page<ExperienceServiceResponse> getExperiencesByFarmId(UUID farmId, Pageable pageable) {
        Page<Experience> experiences = experienceRepository.findByFarmId(farmId, pageable);
        return experiences.map(ExperienceServiceResponse::from);
    }

    /**
     * 모든 체험 프로그램 조회
     *
     * @return 모든 체험 프로그램 목록
     */
    public List<ExperienceServiceResponse> getAllExperiences() {
        List<Experience> experiences = experienceRepository.findAll();
        return experiences.stream().map(ExperienceServiceResponse::from).collect(Collectors.toList());
    }

    /**
     * 모든 체험 프로그램 조회 (페이지네이션)
     *
     * @param pageable 페이지 정보
     * @return 체험 프로그램 페이지
     */
    public Page<ExperienceServiceResponse> getAllExperiences(Pageable pageable) {
        Page<Experience> experiences = experienceRepository.findAll(pageable);
        return experiences.map(ExperienceServiceResponse::from);
    }

    /**
     * 체험 프로그램 수정
     *
     * @param experienceId 체험 ID
     * @param request 체험 프로그램 수정 요청
     * @return 수정된 체험 프로그램
     */
    @Transactional
    public ExperienceServiceResponse updateExperience(UUID experienceId, ExperienceServiceRequest request) {
        Experience existingExperience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new CustomException(ExperienceErrorCode.EXPERIENCE_NOT_FOUND));

        // TODO: 본인 농장의 체험인지 검증
        // - Gateway에서 전달된 X-User-Id 헤더를 확인하여 현재 사용자 ID 획득
        // - existingExperience.getFarmId()와 사용자가 소유한 farmId를 비교
        // - 일치하지 않으면 예외 발생 (예: FARM_FORBIDDEN)

        // 수정 가능한 필드 업데이트
        existingExperience.setTitle(request.getTitle());
        existingExperience.setDescription(request.getDescription());
        existingExperience.setPricePerPerson(request.getPricePerPerson());
        existingExperience.setCapacity(request.getCapacity());
        existingExperience.setDurationMinutes(request.getDurationMinutes());
        existingExperience.setAvailableStartDate(request.getAvailableStartDate());
        existingExperience.setAvailableEndDate(request.getAvailableEndDate());
        existingExperience.setStatus(request.getStatus());

        validateExperience(existingExperience);

        Experience updatedExperience = experienceRepository.save(existingExperience);
        return ExperienceServiceResponse.from(updatedExperience);
    }

    /**
     * 체험 프로그램 삭제
     *
     * @param experienceId 체험 ID
     */
    @Transactional
    public void deleteExperience(UUID experienceId) {
        if (!experienceRepository.existsById(experienceId)) {
            throw new CustomException(ExperienceErrorCode.EXPERIENCE_NOT_FOUND);
        }

        // TODO: 본인 농장의 체험인지 검증
        // - Gateway에서 전달된 X-User-Id 헤더를 확인하여 현재 사용자 ID 획득
        // - experienceId로 체험을 조회하여 farmId 확인
        // - 해당 farmId와 사용자가 소유한 farmId를 비교
        // - 일치하지 않으면 예외 발생 (예: FARM_FORBIDDEN)

        experienceRepository.deleteById(experienceId);
    }

    /**
     * 체험 프로그램 유효성 검증
     *
     * @param experience 검증할 체험 프로그램
     */
    private void validateExperience(Experience experience) {
        if (experience.getAvailableStartDate().isAfter(experience.getAvailableEndDate())) {
            throw new IllegalArgumentException("예약 가능 시작일은 종료일보다 이전이어야 합니다.");
        }

        if (experience.getPricePerPerson().compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("1인당 가격은 0원 이상이어야 합니다.");
        }

        if (experience.getCapacity() < 1) {
            throw new IllegalArgumentException("수용 인원은 1명 이상이어야 합니다.");
        }

        if (experience.getDurationMinutes() < 1) {
            throw new IllegalArgumentException("소요 시간은 1분 이상이어야 합니다.");
        }
    }
}
