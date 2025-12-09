package com.barofarm.support.experience.application.dto;

import com.barofarm.support.experience.domain.Experience;
import com.barofarm.support.experience.domain.ExperienceStatus;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 체험 프로그램 서비스 레이어 응답 DTO */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceServiceResponse {

    private UUID experienceId;
    private UUID farmId;
    private String title;
    private String description;
    private BigInteger pricePerPerson;
    private Integer capacity;
    private Integer durationMinutes;
    private LocalDateTime availableStartDate;
    private LocalDateTime availableEndDate;
    private ExperienceStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Experience 엔티티를 ExperienceServiceResponse로 변환 */
    public static ExperienceServiceResponse from(Experience experience) {
        return new ExperienceServiceResponse(
            experience.getExperienceId(),
            experience.getFarmId(),
            experience.getTitle(),
            experience.getDescription(),
            experience.getPricePerPerson(),
            experience.getCapacity(),
            experience.getDurationMinutes(),
            experience.getAvailableStartDate(),
            experience.getAvailableEndDate(),
            experience.getStatus(),
            experience.getCreatedAt(),
            experience.getUpdatedAt()
        );
    }
}
