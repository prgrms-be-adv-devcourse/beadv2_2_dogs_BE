package com.barofarm.support.event;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceEvent {

    private ExperienceEventType type;
    private ExperienceEventData data;

    public enum ExperienceEventType {
        EXPERIENCE_CREATED,
        EXPERIENCE_UPDATED,
        EXPERIENCE_DELETED
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExperienceEventData {
        private UUID experienceId;
        private String experienceName;
        private Long pricePerPerson;
        private Integer capacity;
        private Integer durationMinutes;
        private LocalDate availableStartDate;
        private LocalDate availableEndDate;
        private String status;
        private Instant updatedAt;
    }
}
