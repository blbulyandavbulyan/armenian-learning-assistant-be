package com.blbulyandavbulyan.larm.api.chat;

import java.util.List;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Examples;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(name = "Dialogue Chat Response")
public record DialogueChatResponse(
        @Schema(description = Descriptions.CHAT_RESPONSE_MESSAGE,
            example = Examples.CHAT_RESPONSE_MESSAGE,
            requiredMode = Schema.RequiredMode.REQUIRED)
        String message,

        DialogueTitleResponse info,

        List<DialoguePhraseResponse> dialoguePhrases) {

    @Schema(name = "Dialogue Title Response")
    @Builder
    record DialogueTitleResponse(
            @Schema(description = Descriptions.SPEAKER_TITLE, example = Examples.SPEAKER_TITLE)
            String title,

            List<TranslationResponse> translations) {

        @Schema(name = "Dialogue Title Translation Response")
        @Builder
        public record TranslationResponse(
                @Schema(description = Descriptions.GENERATED_TRANSLATION_TEXT, example = Examples.TRANSLATION_TEXT)
                String translationText,

                @Schema(description = Descriptions.ISO_LANGUAGE_CODE, example = Examples.TRANSLATION_ISO_LANGUAGE_CODE)
                String isoLanguageCode) {
        }
    }
}
