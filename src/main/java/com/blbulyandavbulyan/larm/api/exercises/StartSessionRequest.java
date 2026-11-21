package com.blbulyandavbulyan.larm.api.exercises;

import java.util.UUID;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "Start Session Request")
public record StartSessionRequest(
        @Schema(description = Descriptions.LEARNING_CARD_ID, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        UUID learningCardId) {
}
