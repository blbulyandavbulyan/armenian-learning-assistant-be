package com.blbulyandavbulyan.larm.api.learning;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Schema(name = "Learning Cards Response")
public record LearningCardsResponse(List<LearningCardResponse> cards) {

    @Schema(name = "Learning Card Response")
    @Builder
    public record LearningCardResponse(
            @Schema(description = Descriptions.LEARNING_CARD_ID, requiredMode = Schema.RequiredMode.REQUIRED)
            UUID learningCardId,

            @Schema(description = Descriptions.LEARNING_ITEM_ID, requiredMode = Schema.RequiredMode.REQUIRED)
            UUID learningItemId,

            @Schema(description = Descriptions.EXERCISE_TYPE, requiredMode = Schema.RequiredMode.REQUIRED)
            ExerciseType exerciseType,

            @Schema(description = Descriptions.VARIANT_ID)
            @Nullable
            UUID variantId,

            @Schema(description = Descriptions.NEXT_REVIEW_AT, requiredMode = Schema.RequiredMode.REQUIRED)
            Instant nextReviewAt,

            @Schema(description = Descriptions.INTERVAL_DAYS, requiredMode = Schema.RequiredMode.REQUIRED)
            int intervalDays,

            @Schema(description = Descriptions.REPETITION_COUNT, requiredMode = Schema.RequiredMode.REQUIRED)
            int repetitionCount,

            @Schema(description = Descriptions.EASE_FACTOR, requiredMode = Schema.RequiredMode.REQUIRED)
            double easeFactor) {
    }
}
