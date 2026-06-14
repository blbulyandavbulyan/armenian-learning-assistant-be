package com.blbulyandavbulyan.larm.ai.chat;

import com.blbulyandavbulyan.larm.validation.ValidIsoLanguageCode;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@Valid
public record DraftTranslationResource(
        @JsonPropertyDescription("Translation text")
        @NotBlank
        String translationText,

        @ValidIsoLanguageCode
        @NotBlank
        @JsonPropertyDescription("iso2 language code of the translationText")
        String isoLanguageCode) {
}
