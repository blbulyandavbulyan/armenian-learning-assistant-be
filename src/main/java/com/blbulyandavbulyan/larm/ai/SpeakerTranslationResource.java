package com.blbulyandavbulyan.larm.ai;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record SpeakerTranslationResource(
        @JsonPropertyDescription("The language code")
        String languageCode,

        @JsonPropertyDescription("The translation value")
        String value) {
}
