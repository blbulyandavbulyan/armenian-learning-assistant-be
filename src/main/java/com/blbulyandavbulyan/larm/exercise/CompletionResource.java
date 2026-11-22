package com.blbulyandavbulyan.larm.exercise;

import java.time.Instant;
import java.util.UUID;

public record CompletionResource(UUID sessionId, Instant completedAt, int aggregateScore, int previousIntervalDays,
                                 int nextIntervalDays, Instant nextReviewAt, double easeFactor) {
}
