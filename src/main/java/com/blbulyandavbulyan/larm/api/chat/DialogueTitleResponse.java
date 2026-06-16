package com.blbulyandavbulyan.larm.api.chat;

import java.util.List;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Examples;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(name = com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.SchemaNames.DIALOGUE_TITLE_RESPONSE)
@Builder
public record DialogueTitleResponse(
        @Schema(description = Descriptions.DIALOGUE_TITLE, example = Examples.DIALOGUE_TITLE,
                requiredMode = Schema.RequiredMode.REQUIRED)
        String title,

        @Schema(description = Descriptions.GENERATED_TRANSCRIPTION, example = Examples.DIALOGUE_TRANSCRIPTION,
                requiredMode = Schema.RequiredMode.REQUIRED)
        String transcription,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<TranslationResponse> translations) {
}
