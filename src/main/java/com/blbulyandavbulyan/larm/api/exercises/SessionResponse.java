package com.blbulyandavbulyan.larm.api.exercises;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Schema(name = "Session Response")
@Builder
public record SessionResponse(
        @Schema(description = Descriptions.SESSION_ID, requiredMode = Schema.RequiredMode.REQUIRED)
        UUID sessionId,

        @Schema(description = Descriptions.LEARNING_CARD_ID, requiredMode = Schema.RequiredMode.REQUIRED)
        UUID learningCardId,

        @Schema(description = Descriptions.LEARNING_ITEM_ID, requiredMode = Schema.RequiredMode.REQUIRED)
        UUID learningItemId,

        @Schema(description = Descriptions.CONTENT_ITEM_ID, requiredMode = Schema.RequiredMode.REQUIRED)
        UUID contentItemId,

        @Schema(description = Descriptions.CONTENT_ITEM_TYPE, requiredMode = Schema.RequiredMode.REQUIRED)
        ContentItemType contentItemType,

        @Schema(description = Descriptions.EXERCISE_TYPE, requiredMode = Schema.RequiredMode.REQUIRED)
        ExerciseType exerciseType,

        @Schema(description = Descriptions.VARIANT_ID)
        @Nullable
        UUID variantId,

        @Schema(description = Descriptions.SUB_ITEM_IDS, requiredMode = Schema.RequiredMode.REQUIRED)
        List<UUID> subItemIds,

        @Schema(description = Descriptions.STARTED_AT, requiredMode = Schema.RequiredMode.REQUIRED)
        Instant startedAt,

        @Schema(description = Descriptions.COMPLETED_AT)
        @Nullable
        Instant completedAt) {
}
