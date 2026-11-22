package com.blbulyandavbulyan.larm.api.exercises;

import java.time.Instant;
import java.util.UUID;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Complete Session Response")
public record CompleteSessionResponse(
        @Schema(description = Descriptions.SESSION_ID, requiredMode = Schema.RequiredMode.REQUIRED)
        UUID sessionId,

        @Schema(description = Descriptions.COMPLETED_AT, requiredMode = Schema.RequiredMode.REQUIRED)
        Instant completedAt,

        @Schema(description = Descriptions.AGGREGATE_SCORE, requiredMode = Schema.RequiredMode.REQUIRED)
        int aggregateScore,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        CardUpdateResponse cardUpdate) {

    @Schema(name = "Card Update Response")
    public record CardUpdateResponse(
            @Schema(description = Descriptions.PREVIOUS_INTERVAL_DAYS, requiredMode = Schema.RequiredMode.REQUIRED)
            int previousIntervalDays,

            @Schema(description = Descriptions.NEXT_INTERVAL_DAYS, requiredMode = Schema.RequiredMode.REQUIRED)
            int nextIntervalDays,

            @Schema(description = Descriptions.NEXT_REVIEW_AT, requiredMode = Schema.RequiredMode.REQUIRED)
            Instant nextReviewAt,

            @Schema(description = Descriptions.EASE_FACTOR, requiredMode = Schema.RequiredMode.REQUIRED)
            double easeFactor) {
    }
}
