package com.barofarm.support.events.search;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
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
    public static class FarmEventData {
        private UUID farmId;
        private String farmName;
        private String farmAddress;
        private String status;
        private Instant updatedAt;
    }
}
