package com.blbulyandavbulyan.larm.ai.chat;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record StructuredPhrasesResource(
        @JsonPropertyDescription("Should contain the response description")
        @NotBlank
        String message,

        @JsonPropertyDescription("The generated phrases")
        @NotEmpty List<@NotNull @Valid DraftPhraseResource> phrases) {
}
