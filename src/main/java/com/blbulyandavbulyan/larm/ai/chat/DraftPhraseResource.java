package com.blbulyandavbulyan.larm.ai.chat;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record DraftPhraseResource(
        @JsonPropertyDescription("The phrase, which will be saved")
        String phrase,


        @JsonPropertyDescription("Transcription of the given phrase")
        String transcription,

        @JsonPropertyDescription("Translations of the given phrase")
        List<DraftTranslationResource> translations) {

    public record DraftTranslationResource(
            @JsonPropertyDescription("Translation text")
            String translationText,

            @JsonPropertyDescription("iso2 language code of the translationText")
            String isoLanguageCode) {
    }
}