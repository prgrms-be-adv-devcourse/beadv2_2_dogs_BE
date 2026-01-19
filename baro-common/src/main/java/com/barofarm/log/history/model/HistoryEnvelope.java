package com.barofarm.log.history.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record HistoryEnvelope<T>(
    HistoryEventType event,
    OffsetDateTime ts,
    UUID userId,
    T payload) {
}
