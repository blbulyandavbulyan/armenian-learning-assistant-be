package com.blbulyandavbulyan.larm.ai.chat;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record StructuredPhrasesResource(
        @JsonPropertyDescription("Should contain the response description")
        String message,

        @JsonPropertyDescription("The generated phrases")
        List<DraftPhraseResource> phrases) {
}
