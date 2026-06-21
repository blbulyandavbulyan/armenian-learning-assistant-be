package com.blbulyandavbulyan.larm.phrase.dao.projections;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.blbulyandavbulyan.larm.phrase.dao.Dialogue;

public record DialogueRecord(
        UUID id,
        PhraseRecord title,
        Set<DialogueSpeakerRecord> speakers,
        Set<DialoguePhraseRecord> dialoguePhrases,
        Instant createdAt) {

    public DialogueRecord(Dialogue dialogue) {
        this(dialogue.getId(), 
                new PhraseRecord(dialogue.getTitle()),
                getSpeakerRecords(dialogue),
                getPhraseRecords(dialogue),
                dialogue.getCreatedAt());
    }

    private static Set<DialogueSpeakerRecord> getSpeakerRecords(Dialogue dialogue) {
        return dialogue.getSpeakers().stream().map(DialogueSpeakerRecord::new).collect(Collectors.toSet());
    }

    private static Set<DialoguePhraseRecord> getPhraseRecords(Dialogue dialogue) {
        return dialogue.getDialoguePhrases().stream().map(DialoguePhraseRecord::new).collect(Collectors.toSet());
    }
}
