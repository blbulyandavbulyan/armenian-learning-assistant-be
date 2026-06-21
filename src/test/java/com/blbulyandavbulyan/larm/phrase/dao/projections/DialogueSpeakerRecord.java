package com.blbulyandavbulyan.larm.phrase.dao.projections;

import java.time.Instant;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.DialogueSpeaker;

public record DialogueSpeakerRecord(
        UUID id,
        String speakerRefId,
        PhraseRecord namePhrase,
        Instant createdAt) {

    public DialogueSpeakerRecord(DialogueSpeaker speaker) {
        this(speaker.getId(), speaker.getSpeakerRefId(), new PhraseRecord(speaker.getNamePhrase()), speaker.getCreatedAt());
    }
}
