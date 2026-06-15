package com.blbulyandavbulyan.larm.api.chat;

import java.util.List;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Examples;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(name = "Dialogue Chat Response")
public record DialogueChatResponse(
        @Schema(description = Descriptions.CHAT_RESPONSE_MESSAGE, example = Examples.CHAT_RESPONSE_MESSAGE,
                requiredMode = Schema.RequiredMode.REQUIRED)
        String message,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        DialogueTitleResponse info,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<SpeakerResponse> speakers,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<DialoguePhraseResponse> dialoguePhrases) {

    @Schema(name = "Dialogue Title Response")
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

        @Schema(name = "Dialogue Title Translation Response")
        @Builder
        public record TranslationResponse(
                @Schema(description = Descriptions.GENERATED_TRANSLATION_TEXT,
                        example = Examples.DIALOGUE_TRANSLATION_TEXT,
                        requiredMode = Schema.RequiredMode.REQUIRED)
                String translationText,

                @Schema(description = Descriptions.ISO_LANGUAGE_CODE,
                        example = Examples.TRANSLATION_ISO_LANGUAGE_CODE,
                        requiredMode = Schema.RequiredMode.REQUIRED)
                String isoLanguageCode) {
        }
    }
}
