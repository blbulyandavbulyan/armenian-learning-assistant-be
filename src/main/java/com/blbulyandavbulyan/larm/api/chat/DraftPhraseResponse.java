package com.blbulyandavbulyan.larm.api.chat;

import java.util.List;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Examples;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(name = "Draft Phrases Response")
@Builder
public record DraftPhraseResponse(
        @Schema(description = Descriptions.GENERATED_PHRASE, example = Examples.PHRASE,
                requiredMode = Schema.RequiredMode.REQUIRED)
        String phrase,

        @Schema(description = Descriptions.GENERATED_TRANSCRIPTION, example = Examples.TRANSCRIPTION,
                requiredMode = Schema.RequiredMode.REQUIRED)
        String transcription,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<TranslationResponse> translations) {

    @Schema(name = "Draft Phrase Translation Response")
    @Builder
    public record TranslationResponse(
            @Schema(description = Descriptions.GENERATED_TRANSLATION_TEXT, example = Examples.TRANSLATION_TEXT,
                    requiredMode = Schema.RequiredMode.REQUIRED)
            String translationText,

            @Schema(description = Descriptions.ISO_LANGUAGE_CODE, example = Examples.TRANSLATION_ISO_LANGUAGE_CODE,
                    requiredMode = Schema.RequiredMode.REQUIRED)
            String isoLanguageCode) {
    }
}
