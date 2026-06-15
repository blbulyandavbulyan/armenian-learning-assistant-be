package com.blbulyandavbulyan.larm.api.chat;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Examples;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(name = "Dialogue Phrase Response")
public record DialoguePhraseResponse(
        @Schema(description = Descriptions.SPEAKER_ID, example = Examples.SPEAKER_ID,
                requiredMode = Schema.RequiredMode.REQUIRED)
        String speakerId,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        DraftPhraseResponse phrase) {
}
