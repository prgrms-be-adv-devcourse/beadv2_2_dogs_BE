package com.barofarm.support.experience.application;

import com.barofarm.support.common.client.FarmClient;
import com.barofarm.support.common.exception.CustomException;
import com.barofarm.support.experience.application.dto.ExperienceServiceRequest;
import com.barofarm.support.experience.application.dto.ExperienceServiceResponse;
import com.barofarm.support.experience.domain.Experience;
import com.barofarm.support.experience.domain.ExperienceRepository;
import com.barofarm.support.experience.exception.ExperienceErrorCode;
import java.math.BigInteger;
import java.util.UUID;
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
    private final FarmClient farmClient;

    /**
     * 체험 프로그램 ID로 조회 (null 체크 및 존재 여부 검증 포함)
     *
     * @param experienceId 체험 ID
     * @return 체험 프로그램 엔티티
     * @throws CustomException 체험 ID가 null이거나 존재하지 않는 경우
     */
    private Experience findExperienceById(UUID experienceId) {
        if (experienceId == null) {
            throw new CustomException(ExperienceErrorCode.EXPERIENCE_NOT_FOUND);
        }

        return experienceRepository.findById(experienceId)
                .orElseThrow(() -> new CustomException(ExperienceErrorCode.EXPERIENCE_NOT_FOUND));
    }

    /**
     * 체험 프로그램 권한 검증
     *
     * @param experience 검증할 체험 프로그램
     * @param userFarmId 사용자가 소유한 농장 ID
     * @throws CustomException 권한이 없는 경우
     */
    private void validateAccess(Experience experience, UUID userFarmId) {
        if (!experience.getFarmId().equals(userFarmId)) {
            throw new CustomException(ExperienceErrorCode.ACCESS_DENIED);
        }
    }

    /**
     * 체험 프로그램 데이터 유효성 검증
     *
     * @param experience 검증할 체험 프로그램
     * @throws CustomException 검증 실패 시
     */
    private void validateExperience(Experience experience) {
        if (experience.getAvailableStartDate().isAfter(experience.getAvailableEndDate())) {
            throw new CustomException(ExperienceErrorCode.INVALID_DATE_RANGE);
        }

        if (experience.getPricePerPerson().compareTo(BigInteger.ZERO) < 0) {
            throw new CustomException(ExperienceErrorCode.INVALID_PRICE);
        }

        if (experience.getCapacity() < 1) {
            throw new CustomException(ExperienceErrorCode.INVALID_CAPACITY);
        }

        if (experience.getDurationMinutes() < 1) {
            throw new CustomException(ExperienceErrorCode.INVALID_DURATION);
        }
    }

    /**
     * 체험 프로그램 생성
     *
     * @param userId 사용자 ID
     * @param request 체험 프로그램 생성 요청
     * @return 생성된 체험 프로그램
     */
    @Transactional
    public ExperienceServiceResponse createExperience(UUID userId, ExperienceServiceRequest request) {
        // Feign 클라이언트를 통해 사용자가 소유한 farmId 조회
        // 현재 통신은 구현/테스트되지 않았음, Mock으로 테스트 진행
        UUID userFarmId = farmClient.getFarmIdByUserId(userId);

        Experience experience = request.toEntity();
        validateAccess(experience, userFarmId);
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
        Experience experience = findExperienceById(experienceId);
        return ExperienceServiceResponse.from(experience);
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
     * 사용자 ID로 본인 농장의 체험 프로그램 목록 조회 (페이지네이션)
     *
     * @param userId 사용자 ID
     * @param pageable 페이지 정보
     * @return 체험 프로그램 페이지
     */
    public Page<ExperienceServiceResponse> getMyExperiences(UUID userId, Pageable pageable) {
        // Feign 클라이언트를 통해 사용자가 소유한 farmId 조회
        // 현재 통신은 구현/테스트되지 않았음, Mock으로 테스트 진행
        UUID farmId = farmClient.getFarmIdByUserId(userId);

        // farmId로 체험 목록 조회
        Page<Experience> experiences = experienceRepository.findByFarmId(farmId, pageable);
        return experiences.map(ExperienceServiceResponse::from);
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
     * @param userId 사용자 ID
     * @param experienceId 체험 ID
     * @param request 체험 프로그램 수정 요청
     * @return 수정된 체험 프로그램
     */
    @Transactional
    public ExperienceServiceResponse updateExperience(
            UUID userId, UUID experienceId, ExperienceServiceRequest request) {
        Experience existingExperience = findExperienceById(experienceId);

        // Feign 클라이언트를 통해 사용자가 소유한 farmId 조회
        // 현재 통신은 구현/테스트되지 않았음, Mock으로 테스트 진행
        UUID userFarmId = farmClient.getFarmIdByUserId(userId);

        validateAccess(existingExperience, userFarmId);

        existingExperience.update(
                request.getTitle(),
                request.getDescription(),
                request.getPricePerPerson(),
                request.getCapacity(),
                request.getDurationMinutes(),
                request.getAvailableStartDate(),
                request.getAvailableEndDate(),
                request.getStatus()
        );

        validateExperience(existingExperience);

        // JPA 더티 체킹, @Transactional 종료 시 자동으로 변경사항이 DB에 반영됨
        // 멘토님 추천
        return ExperienceServiceResponse.from(existingExperience);
    }

    /**
     * 체험 프로그램 삭제
     *
     * @param userId 사용자 ID
     * @param experienceId 체험 ID
     */
    @Transactional
    public void deleteExperience(UUID userId, UUID experienceId) {
        Experience experience = findExperienceById(experienceId);

        // Feign 클라이언트를 통해 사용자가 소유한 farmId 조회
        // 현재 통신은 구현/테스트되지 않았음, Mock으로 테스트 진행
        UUID userFarmId = farmClient.getFarmIdByUserId(userId);

        validateAccess(experience, userFarmId);

        experienceRepository.deleteById(experienceId);
    }

}
