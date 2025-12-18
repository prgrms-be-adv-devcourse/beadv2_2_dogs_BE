package com.barofarm.support.search.experience.application.dto;

import java.util.UUID;

public record ExperienceAutoItem(
    UUID experienceId, // 프론트에서 클릭 시 체험으로 바로 이동
    String experienceName, // 체험명
    String type // 타입
) {
    public ExperienceAutoItem(UUID experienceId, String experienceName) {
        this(experienceId, experienceName, "experience");
    }
}
