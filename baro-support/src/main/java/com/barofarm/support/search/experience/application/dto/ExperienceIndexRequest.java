package com.barofarm.support.search.experience.application.dto;

import java.time.LocalDate;
import java.util.UUID;

// 체험 색인 요청 DTO (updatedAt은 서버에서 자동 생성)
public record ExperienceIndexRequest(
    UUID experienceId,
    String experienceName,
    Long pricePerPerson,
    Integer capacity,
    Integer durationMinutes,
    LocalDate availableStartDate,
    LocalDate availableEndDate,
    String status) {
}
