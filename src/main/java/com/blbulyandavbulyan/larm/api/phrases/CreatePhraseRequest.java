package com.blbulyandavbulyan.larm.api.phrases;

import java.util.List;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Examples;
import com.blbulyandavbulyan.larm.validation.ValidIsoLanguageCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(name = "Create Phrase Request")
record CreatePhraseRequest(
        @Schema(description = Descriptions.APPROVED_PHRASE, example = Examples.PHRASE)
        @NotBlank
        String phrase,

        @Schema(description = Descriptions.APPROVED_TRANSCRIPTION, example = Examples.TRANSCRIPTION)
        @NotBlank
        String transcription,

        @NotEmpty
        List<@NotNull CreateTranslationRequest> translations) {

    @Schema(name = "Phrase Create Translation Request")
    public record CreateTranslationRequest(
            @Schema(description = Descriptions.APPROVED_TRANSLATION_TEXT, example = Examples.TRANSLATION_TEXT)
            @NotBlank
            String translationText,

            @Schema(description = Descriptions.ISO_LANGUAGE_CODE, example = Examples.TRANSLATION_ISO_LANGUAGE_CODE)
            @NotBlank
            @ValidIsoLanguageCode
            String isoLanguageCode) {
    }
}
