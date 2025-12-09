package com.barofarm.support.experience.application.dto;

import com.barofarm.support.experience.domain.Experience;
import com.barofarm.support.experience.domain.ExperienceStatus;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 체험 프로그램 서비스 레이어 요청 DTO */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceServiceRequest {

    private UUID farmId;
    private String title;
    private String description;
    private BigInteger pricePerPerson;
    private Integer capacity;
    private Integer durationMinutes;
    private LocalDateTime availableStartDate;
    private LocalDateTime availableEndDate;
    private ExperienceStatus status;

    /** DTO를 엔티티로 변환 */
    public Experience toEntity() {
        return new Experience(null, farmId, title, description, pricePerPerson, capacity, durationMinutes,
            availableStartDate, availableEndDate, status);
    }
}
