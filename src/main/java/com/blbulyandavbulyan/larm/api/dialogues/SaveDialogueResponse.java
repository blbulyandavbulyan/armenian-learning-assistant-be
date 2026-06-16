package com.blbulyandavbulyan.larm.api.dialogues;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Save Dialogue Response")
record SaveDialogueResponse(
        @Schema(description = "The UUID of the saved dialogue", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id) {
}
