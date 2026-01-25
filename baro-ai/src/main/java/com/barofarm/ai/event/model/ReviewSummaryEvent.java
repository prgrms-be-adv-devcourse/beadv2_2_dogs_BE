package com.barofarm.ai.event.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReviewSummaryEvent(UUID productId,
                                 String sentiment,
                                 List<String> summaryText,
                                 Instant updatedAt) {
}
