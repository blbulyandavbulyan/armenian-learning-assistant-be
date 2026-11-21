package com.blbulyandavbulyan.larm.api.learning;

import java.time.Instant;
import java.util.UUID;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Schema(name = "Learning Item Response")
@Builder
public record LearningItemResponse(
        @Schema(description = Descriptions.LEARNING_ITEM_ID, requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,

        @Schema(description = Descriptions.CONTENT_ITEM_ID, requiredMode = Schema.RequiredMode.REQUIRED)
        UUID contentItemId,

        @Schema(description = Descriptions.CONTENT_ITEM_TYPE, requiredMode = Schema.RequiredMode.REQUIRED)
        ContentItemType contentItemType,

        @Schema(description = Descriptions.ENROLLED_AT, requiredMode = Schema.RequiredMode.REQUIRED)
        Instant enrolledAt,

        @Schema(description = Descriptions.EXCLUDED_AT)
        @Nullable
        Instant excludedAt) {
}
