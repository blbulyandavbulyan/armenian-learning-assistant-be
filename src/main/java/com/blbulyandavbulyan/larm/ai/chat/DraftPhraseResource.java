package com.blbulyandavbulyan.larm.ai.chat;

import java.util.List;

import com.blbulyandavbulyan.larm.dialogue.DraftGeneratedDialogue;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record DraftPhraseResource(
        @JsonPropertyDescription("The phrase, which will be saved")
        String phrase,

        @JsonPropertyDescription("Iso language code for the phrase")
        String isoLanguageCode,

        @JsonPropertyDescription("Transcription of the given phrase")
        String transcription,

        @JsonPropertyDescription("Translations of the given phrase")
        List<DraftTranslationResource> translations) implements DraftGeneratedDialogue.DraftPhrase {

}
