package com.blbulyandavbulyan.larm.ai;

import java.util.List;

import com.blbulyandavbulyan.larm.ai.chat.DraftPhraseResource;
import com.blbulyandavbulyan.larm.ai.chat.DraftTranslationResource;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
@Valid
public record StructuredDialogueResource(
        @JsonPropertyDescription("Should contain the response description")
        @NotBlank
        String message,

        @NotNull
        @Valid
        @JsonPropertyDescription("The info of the dialogue")
        DialogueTitleResource info,

        @JsonPropertyDescription("The list of speakers participating in the dialogue")
        @NotEmpty
        List<@NotNull @Valid SpeakerResource> speakers,

        @JsonPropertyDescription("The sequential phrases forming the dialogue")
        @NotEmpty
        List<@NotNull @Valid DialoguePhrase> dialoguePhrases) {

    @Valid
    public record DialogueTitleResource(
            @JsonPropertyDescription("The title text in Armenian")
            @NotBlank
            String title,

            @JsonPropertyDescription("The transcription of the title")
            @NotBlank
            String transcription,

            @JsonPropertyDescription("Translations of the title")
            @NotEmpty
            List<@NotNull @Valid DraftTranslationResource> translations) {

    }

    @Valid
    public record SpeakerResource(
            @JsonPropertyDescription("A unique identifier for the speaker, e.g. 'speaker1'")
            @NotBlank
            String id,

            @JsonPropertyDescription("The speaker title")
            @NotBlank
            String title,

            @NotBlank
            String transcription,

            @JsonPropertyDescription("translations for the speaker title")
            @NotEmpty
            List<@NotNull @Valid DraftTranslationResource> translations) {

    }

    @Valid
    public record DialoguePhrase(
            @JsonPropertyDescription("The id of the speaker saying this phrase")
            @NotBlank
            String speakerId,

            @Valid
            @NotNull
            DraftPhraseResource phrase) {
    }

}
