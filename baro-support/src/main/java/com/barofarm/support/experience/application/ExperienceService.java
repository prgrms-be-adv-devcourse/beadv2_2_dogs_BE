package com.barofarm.support.experience.application;

import com.barofarm.support.experience.application.dto.ExperienceServiceRequest;
import com.barofarm.support.experience.application.dto.ExperienceServiceResponse;
import com.barofarm.support.experience.domain.Experience;
import com.barofarm.support.experience.domain.ExperienceRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
        Experience experience = request.toEntity();
        validateExperience(experience);
        Experience savedExperience = experienceRepository.save(experience);
        return ExperienceServiceResponse.from(savedExperience);
    }

    /**
     * ID로 체험 프로그램 조회
     *
     * @param id
     *            체험 ID
     * @return 체험 프로그램
     */
    public ExperienceServiceResponse getExperienceById(Long id) {
        Experience experience = experienceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("체험 프로그램을 찾을 수 없습니다. ID: " + id));
        return ExperienceServiceResponse.from(experience);
    }

    /**
     * 농장 ID로 체험 프로그램 목록 조회
     *
     * @param farmId
     *            농장 ID
     * @return 체험 프로그램 목록
     */
    public List<ExperienceServiceResponse> getExperiencesByFarmId(Long farmId) {
        List<Experience> experiences = experienceRepository.findByFarmId(farmId);
        return experiences.stream().map(ExperienceServiceResponse::from).collect(Collectors.toList());
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
     * 체험 프로그램 수정
     *
     * @param id
     *            체험 ID
     * @param request
     *            체험 프로그램 수정 요청
     * @return 수정된 체험 프로그램
     */
    @Transactional
    public ExperienceServiceResponse updateExperience(Long id, ExperienceServiceRequest request) {
        Experience existingExperience = experienceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("체험 프로그램을 찾을 수 없습니다. ID: " + id));

        // 수정 가능한 필드 업데이트
        existingExperience.setTitle(request.getTitle());
        existingExperience.setDescription(request.getDescription());
        existingExperience.setPrice(request.getPrice());
        existingExperience.setMaxParticipants(request.getMaxParticipants());
        existingExperience.setStartDate(request.getStartDate());
        existingExperience.setEndDate(request.getEndDate());

        validateExperience(existingExperience);

        Experience updatedExperience = experienceRepository.save(existingExperience);
        return ExperienceServiceResponse.from(updatedExperience);
    }

    /**
     * 체험 프로그램 삭제
     *
     * @param id
     *            체험 ID
     */
    @Transactional
    public void deleteExperience(Long id) {
        if (!experienceRepository.existsById(id)) {
            throw new IllegalArgumentException("체험 프로그램을 찾을 수 없습니다. ID: " + id);
        }
        experienceRepository.deleteById(id);
    }

    /**
     * 체험 프로그램 유효성 검증
     *
     * @param experience
     *            검증할 체험 프로그램
     */
    private void validateExperience(Experience experience) {
        if (experience.getStartDate().isAfter(experience.getEndDate())) {
            throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 이전이어야 합니다.");
        }

        if (experience.getPrice() < 0) {
            throw new IllegalArgumentException("가격은 0원 이상이어야 합니다.");
        }

        if (experience.getMaxParticipants() < 1) {
            throw new IllegalArgumentException("최대 참가자 수는 1명 이상이어야 합니다.");
        }
    }
}
