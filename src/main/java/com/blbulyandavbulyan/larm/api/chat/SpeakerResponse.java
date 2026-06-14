package com.blbulyandavbulyan.larm.api.chat;

import java.util.List;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Examples;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(name = "Speakers Response")
public record SpeakerResponse(
        @Schema(description = Descriptions.SPEAKER_TITLE, example = Examples.SPEAKER_TITLE)
        String title,

        @Schema(description = Descriptions.GENERATED_TRANSCRIPTION, example = Examples.SPEAKER_TRANSCRIPTION)
        String transcription,


        List<TranslationResponse> translations) {

    @Schema(name = "Speaker Translation Response")
    @Builder
    public record TranslationResponse(
            @Schema(description = Descriptions.GENERATED_TRANSLATION_TEXT, example = Examples.SPEAKER_TRANSLATION_TEXT,
                    requiredMode = Schema.RequiredMode.REQUIRED)
            String translationText,

            @Schema(description = Descriptions.ISO_LANGUAGE_CODE, example = Examples.TRANSLATION_ISO_LANGUAGE_CODE,
                    requiredMode = Schema.RequiredMode.REQUIRED)
            String isoLanguageCode) {
    }
}
