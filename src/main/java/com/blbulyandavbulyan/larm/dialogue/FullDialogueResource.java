package com.blbulyandavbulyan.larm.dialogue;

import java.util.List;
import java.util.UUID;

import com.blbulyandavbulyan.larm.phrase.PhraseResource;
import lombok.Builder;

@Builder
public record FullDialogueResource(
        UUID id,
        PhraseResource title,
        List<DialogueSpeakerResource> speakers,
        List<DialoguePhraseResource> dialoguePhrases
) {
    @Builder
    public record DialogueSpeakerResource(
            String speakerRefId,
            PhraseResource name
    ) {
    }

    @Builder
    public record DialoguePhraseResource(
            String speakerRefId,
            PhraseResource phrase
    ) {
    }
}
