package com.blbulyandavbulyan.larm.ai.chat;


import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record StructuredPhrasesResource(
        @JsonPropertyDescription("Should contain the response description")
        String message,

        @JsonPropertyDescription("The generated phrases")
        List<DraftPhraseResource> phrases) {
}
