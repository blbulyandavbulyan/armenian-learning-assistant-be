package com.blbulyandavbulyan.larm.ai.chat;

import com.blbulyandavbulyan.larm.dialogue.DraftGeneratedDialogue;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record DraftTranslationResource(
        @JsonPropertyDescription("Translation text")
        String translationText,

        @JsonPropertyDescription("iso2 language code of the translationText")
        String isoLanguageCode) implements DraftGeneratedDialogue.DraftTranslation {
}
