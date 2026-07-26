package com.blbulyandavbulyan.larm.ai.chat;

import java.util.List;

import com.blbulyandavbulyan.larm.dialogue.DraftGeneratedDialogue;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Builder;

@Builder
public record StructuredDialogueResource(
        @JsonPropertyDescription("Should contain the response description")
        String message,

        @JsonPropertyDescription("The info of the dialogue")
        DialogueTitleResource info,

        @JsonPropertyDescription("The list of speakers participating in the dialogue")
        List<SpeakerResource> speakers,

        @JsonPropertyDescription("The sequential phrases forming the dialogue")
        List<DialoguePhrase> dialoguePhrases) implements DraftGeneratedDialogue {

    public record DialogueTitleResource(
            @JsonPropertyDescription("The title text in Armenian")
            String title,

            @JsonPropertyDescription("The transcription of the title")
            String transcription,

            @JsonPropertyDescription("Translations of the title")
            List<DraftTranslationResource> translations) implements DraftGeneratedDialogue.DraftDialogueTitle {

    }

    public record SpeakerResource(
            @JsonPropertyDescription("A unique identifier for the speaker, e.g. 'speaker1'")
            String id,

            @JsonPropertyDescription("The speaker title")
            String title,

            @JsonPropertyDescription("The transcription of the speaker title in English letters")
            String transcription,

            @JsonPropertyDescription("translations for the speaker title")
            List<DraftTranslationResource> translations) implements DraftGeneratedDialogue.DraftSpeaker {

    }

    public record DialoguePhrase(
            @JsonPropertyDescription("The id of the speaker saying this phrase")
            String speakerId,

            DraftPhraseResource phrase) implements DraftGeneratedDialogue.DraftDialoguePhrase {
    }

}
