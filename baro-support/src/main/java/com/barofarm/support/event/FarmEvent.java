package com.barofarm.support.event;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmEvent {

    private FarmEventType type;
    private FarmEventData data;

    public enum FarmEventType {
        FARM_CREATED,
        FARM_UPDATED,
        FARM_DELETED
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FarmEventData {
        private UUID farmId;
        private String farmName;
        private String farmAddress;
        private String status;
        private Instant updatedAt;
    }
}
