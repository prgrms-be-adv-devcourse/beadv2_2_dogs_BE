package com.barofarm.support.search.experience.application.dto;

import java.util.UUID;

// 프론트에 보여줄 체험 List Item
public record ExperienceSearchItem(
    UUID experienceId,
    String experienceName, // 체험명
    Long pricePerPerson, // 1인당 가격
    Integer capacity, // 수용 인원
    Integer durationMinutes // 소요 시간
) {}
