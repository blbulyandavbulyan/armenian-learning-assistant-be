package com.blbulyandavbulyan.larm.ai.tools;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Builder;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Builder
public record BatchSavePhraseToolResult(
        @JsonPropertyDescription("Contains existing phrases which were already present in the DB, so they were not saved")
        Set<String> existingPhrases,

        @JsonPropertyDescription("Contains successfully saved phrases")
        List<PhraseSaveResult> savedPhrases) {

    @Builder
    public record PhraseSaveResult(
            @JsonPropertyDescription("Id of the phrase")
            UUID id,

            @JsonPropertyDescription("The phrase for which saving operation was triggered")
            String phrase) {

    }
}
