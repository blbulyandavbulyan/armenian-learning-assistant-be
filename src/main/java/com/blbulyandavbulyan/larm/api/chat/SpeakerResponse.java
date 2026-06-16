package com.blbulyandavbulyan.larm.api.chat;

import java.util.List;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Examples;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(name = com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.SchemaNames.SPEAKER_RESPONSE)
public record SpeakerResponse(
        @Schema(description = Descriptions.SPEAKER_ID, example = Examples.SPEAKER_ID,
                requiredMode = Schema.RequiredMode.REQUIRED)
        String id,

        @Schema(description = Descriptions.SPEAKER_TITLE, example = Examples.SPEAKER_TITLE,
                requiredMode = Schema.RequiredMode.REQUIRED)
        String title,

        @Schema(description = Descriptions.GENERATED_TRANSCRIPTION, example = Examples.SPEAKER_TRANSCRIPTION,
                requiredMode = Schema.RequiredMode.REQUIRED)
        String transcription,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<TranslationResponse> translations) {
}
