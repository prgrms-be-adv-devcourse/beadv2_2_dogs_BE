package com.barofarm.support.experience.application.dto;

import com.barofarm.support.experience.domain.Experience;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 체험 프로그램 서비스 레이어 응답 DTO */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceServiceResponse {

    private Long id;
    private Long farmId;
    private String title;
    private String description;
    private Integer price;
    private Integer maxParticipants;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Experience 엔티티를 ExperienceServiceResponse로 변환 */
    public static ExperienceServiceResponse from(Experience experience) {
        return new ExperienceServiceResponse(experience.getId(), experience.getFarmId(), experience.getTitle(),
                experience.getDescription(), experience.getPrice(), experience.getMaxParticipants(),
                experience.getStartDate(), experience.getEndDate(), experience.getCreatedAt(),
                experience.getUpdatedAt());
    }
}
