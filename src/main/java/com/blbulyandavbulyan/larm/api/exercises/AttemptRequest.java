package com.blbulyandavbulyan.larm.api.exercises;

import java.util.UUID;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Schema(name = "Attempt Request")
public record AttemptRequest(
        @Schema(description = Descriptions.SUB_ITEM_ID, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        UUID contentSubItemId,

        @Schema(description = Descriptions.USED_HINT)
        boolean usedHint,

        @Schema(description = Descriptions.ANSWER, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Valid
        AnswerRequest answer) {
}
