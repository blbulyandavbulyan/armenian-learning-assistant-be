package com.blbulyandavbulyan.larm.api.dialogues;

import java.util.UUID;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Save Dialogue Response")
record SaveDialogueResponse(
        @Schema(description = Descriptions.DIALOGUE_ID, requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id) {
}
