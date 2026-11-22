package com.blbulyandavbulyan.larm.api.exercises;

import java.util.UUID;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Schema(name = "Attempt Response")
@Builder
public record AttemptResponse(
        @Schema(description = Descriptions.ATTEMPT_ID, requiredMode = Schema.RequiredMode.REQUIRED)
        UUID attemptId,

        @Schema(description = Descriptions.SUB_ITEM_ID, requiredMode = Schema.RequiredMode.REQUIRED)
        UUID contentSubItemId,

        @Schema(description = Descriptions.USED_HINT, requiredMode = Schema.RequiredMode.REQUIRED)
        boolean usedHint,

        @Schema(description = Descriptions.QUALITY, requiredMode = Schema.RequiredMode.REQUIRED)
        int quality,

        @Schema(description = Descriptions.CORRECT, requiredMode = Schema.RequiredMode.REQUIRED)
        boolean correct,

        @Schema(description = Descriptions.EXPECTED_TEXT)
        @Nullable
        String expectedText) {
}
