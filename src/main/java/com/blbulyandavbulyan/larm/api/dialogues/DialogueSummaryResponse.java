package com.blbulyandavbulyan.larm.api.dialogues;

import java.util.UUID;

import com.blbulyandavbulyan.larm.api.phrases.PhraseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import static com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import static com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Examples;

@Builder
@Schema(name = "Dialogue Summary Response")
public record DialogueSummaryResponse(
        @Schema(description = Descriptions.DIALOGUE_ID, examples = Examples.DIALOGUE_ID, requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,

        PhraseResponse title) {
}
