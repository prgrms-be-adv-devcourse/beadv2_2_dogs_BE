package com.barofarm.support.experience.application;

import com.barofarm.exception.CustomException;
import com.barofarm.support.experience.application.dto.ExperienceServiceRequest;
import com.barofarm.support.experience.application.dto.ExperienceServiceResponse;
import com.barofarm.support.experience.application.event.ExperienceTransactionEvent;
import com.barofarm.support.experience.domain.Experience;
import com.barofarm.support.experience.domain.ExperienceRepository;
import com.barofarm.support.experience.exception.ExperienceErrorCode;
import com.barofarm.support.experience.infrastructure.cache.FarmCacheService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final FarmCacheService farmCacheService;
    private final ApplicationEventPublisher applicationEventPublisher;

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
     * 사용자가 여러 farm을 소유할 수 있으므로, 체험의 farmId를 소유하고 있는지 확인
     *
     * @param experience 검증할 체험 프로그램
     * @param userId 사용자 ID
     * @throws CustomException 권한이 없는 경우
     */
    private void validateAccess(Experience experience, UUID userId) {
        UUID experienceFarmId = experience.getFarmId();
        if (!farmCacheService.hasFarmAccess(userId, experienceFarmId)) {
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

        if (experience.getPricePerPerson() < 0) {
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
     * 사용자 ID로 농장 ID 조회 (Redis 캐시 우선)
     * farmId가 지정된 경우 해당 farm을 소유하고 있는지 확인
     *
     * @param userId 사용자 ID
     * @param farmId 확인할 farm ID (null이면 첫 번째 farm 반환)
     * @return 농장 ID 또는 null
     */
    private UUID getUserFarmIdOrNull(UUID userId, UUID farmId) {
        return farmCacheService.getFarmIdByUserId(userId, farmId);
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
        Experience experience = request.toEntity();
        // 체험의 farmId를 소유하고 있는지 확인 (여러 farm 소유 가능)
        validateAccess(experience, userId);
        validateExperience(experience);
        Experience savedExperience = experienceRepository.save(experience);

        // 트랜잭션 이벤트 발행
        ExperienceTransactionEvent event = new ExperienceTransactionEvent(savedExperience,
            ExperienceTransactionEvent.ExperienceOperation.CREATED);
        applicationEventPublisher.publishEvent(event);

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
     * @param farmId 선택적으로 전달되는 농장 ID (null이면 사용자 ID로 조회)
     * @param pageable 페이지 정보
     * @return 체험 프로그램 페이지
     */
    public Page<ExperienceServiceResponse> getMyExperiences(UUID userId, UUID farmId, Pageable pageable) {
        // farmId가 지정된 경우 소유 여부 확인, 없으면 첫 번째 farm 조회
        // FarmCacheService가 farmId 소유 여부를 확인하고 반환 (보안 검증 포함)
        UUID effectiveFarmId = getUserFarmIdOrNull(userId, farmId);
        // seller-service에 농장이 없거나, 지정된 farmId를 소유하지 않는 경우 빈 페이지를 반환한다.
        if (effectiveFarmId == null) {
            return Page.empty(pageable);
        }

        // farmId로 체험 목록 조회
        Page<Experience> experiences = experienceRepository.findByFarmId(effectiveFarmId, pageable);
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

        // 체험의 farmId를 소유하고 있는지 확인 (여러 farm 소유 가능)
        validateAccess(existingExperience, userId);

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

        // 트랜잭션 이벤트 발행
        ExperienceTransactionEvent event = new ExperienceTransactionEvent(existingExperience,
            ExperienceTransactionEvent.ExperienceOperation.UPDATED);
        applicationEventPublisher.publishEvent(event);

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

        // 체험의 farmId를 소유하고 있는지 확인 (여러 farm 소유 가능)
        validateAccess(experience, userId);

        experienceRepository.deleteById(experienceId);

        // 트랜잭션 이벤트 발행
        ExperienceTransactionEvent event = new ExperienceTransactionEvent(experience,
            ExperienceTransactionEvent.ExperienceOperation.DELETED);
        applicationEventPublisher.publishEvent(event);
    }

}
