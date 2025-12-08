package com.barofarm.support.experience.application.dto;

import com.barofarm.support.experience.domain.Experience;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 체험 프로그램 서비스 레이어 요청 DTO */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceServiceRequest {

    private Long farmId;
    private String title;
    private String description;
    private Integer price;
    private Integer maxParticipants;
    private LocalDate startDate;
    private LocalDate endDate;

    /** DTO를 엔티티로 변환 */
    public Experience toEntity() {
        return new Experience(farmId, title, description, price, maxParticipants, startDate, endDate);
    }
}
