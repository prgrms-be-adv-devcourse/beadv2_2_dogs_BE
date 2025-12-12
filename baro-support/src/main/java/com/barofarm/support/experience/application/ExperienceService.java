package com.barofarm.support.experience.application;

import com.barofarm.support.common.exception.CustomException;
import com.barofarm.support.experience.application.dto.ExperienceServiceRequest;
import com.barofarm.support.experience.application.dto.ExperienceServiceResponse;
import com.barofarm.support.experience.domain.Experience;
import com.barofarm.support.experience.domain.ExperienceRepository;
import com.barofarm.support.experience.exception.ExperienceErrorCode;
import com.barofarm.support.common.client.FarmClient;
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

        // request.getFarmId()와 사용자가 소유한 farmId를 비교하여 본인 농장인지 검증
        if (!request.getFarmId().equals(userFarmId)) {
            throw new CustomException(ExperienceErrorCode.ACCESS_DENIED);
        }

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
        if (experienceId == null) {
            throw new CustomException(ExperienceErrorCode.EXPERIENCE_NOT_FOUND);
        }

        Experience experience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new CustomException(ExperienceErrorCode.EXPERIENCE_NOT_FOUND));

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
    public ExperienceServiceResponse updateExperience(UUID userId, UUID experienceId, ExperienceServiceRequest request) {
        Experience existingExperience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new CustomException(ExperienceErrorCode.EXPERIENCE_NOT_FOUND));

        // Feign 클라이언트를 통해 사용자가 소유한 farmId 조회
        // 현재 통신은 구현/테스트되지 않았음, Mock으로 테스트 진행
        UUID userFarmId = farmClient.getFarmIdByUserId(userId);

        // existingExperience.getFarmId()와 사용자가 소유한 farmId를 비교
        if (!existingExperience.getFarmId().equals(userFarmId)) {
            throw new CustomException(ExperienceErrorCode.ACCESS_DENIED);
        }

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
        Experience experience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new CustomException(ExperienceErrorCode.EXPERIENCE_NOT_FOUND));

        // Feign 클라이언트를 통해 사용자가 소유한 farmId 조회
        // 현재 통신은 구현/테스트되지 않았음, Mock으로 테스트 진행
        UUID userFarmId = farmClient.getFarmIdByUserId(userId);

        // 해당 farmId와 사용자가 소유한 farmId를 비교
        if (!experience.getFarmId().equals(userFarmId)) {
            throw new CustomException(ExperienceErrorCode.ACCESS_DENIED);
        }

        experienceRepository.deleteById(experienceId);
    }

}
