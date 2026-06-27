package com.blbulyandavbulyan.larm.phrase.dao.projections;

import java.time.Instant;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.DialogueSpeaker;

public record DialogueSpeakerRecord(
        UUID id,
        PhraseRecord namePhrase,
        Instant createdAt) {

    public DialogueSpeakerRecord(DialogueSpeaker speaker) {
        this(speaker.getId(), new PhraseRecord(speaker.getNamePhrase()), speaker.getCreatedAt());
    }
}
