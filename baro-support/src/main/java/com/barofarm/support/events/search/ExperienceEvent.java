package com.barofarm.support.events.search;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
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
