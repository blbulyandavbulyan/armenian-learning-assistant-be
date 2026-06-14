package com.blbulyandavbulyan.larm.ai.chat;

import java.util.List;

import com.blbulyandavbulyan.larm.validation.ValidIsoLanguageCode;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Valid
public record DraftPhraseResource(
        @JsonPropertyDescription("The phrase, which will be saved")
        @NotBlank
        String phrase,

        @JsonPropertyDescription("Iso language code for the phrase")
        @ValidIsoLanguageCode(supportedLanguages = {"hy"})
        @NotBlank
        String isoLanguageCode,

        @JsonPropertyDescription("Transcription of the given phrase")
        @NotBlank
        String transcription,

        @JsonPropertyDescription("Translations of the given phrase")
        @NotEmpty List<@NotNull @Valid DraftTranslationResource> translations) {

}