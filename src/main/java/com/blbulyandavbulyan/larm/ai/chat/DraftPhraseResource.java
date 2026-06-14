package com.blbulyandavbulyan.larm.ai.chat;

import java.util.List;
import java.util.Locale;

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

        //TODO (), check if adding it in description changes anything
        @JsonPropertyDescription("Iso language code for the phrase, should be 'ru', only this one is supported here")
        @ValidIsoLanguageCode(supportedLanguages = {"hy"})
        @NotBlank
        String isoLanguageCode,

        @JsonPropertyDescription("Transcription of the given phrase")
        @NotBlank
        String transcription,

        @JsonPropertyDescription("Translations of the given phrase")
        @NotEmpty List<@NotNull @Valid DraftTranslationResource> translations) {

}