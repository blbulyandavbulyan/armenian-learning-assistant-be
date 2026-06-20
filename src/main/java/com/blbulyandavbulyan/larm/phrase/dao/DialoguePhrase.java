package com.blbulyandavbulyan.larm.phrase.dao;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

@Builder
@Table("dialogue_phrases")
public record DialoguePhrase(
        @Id UUID id,
        UUID dialogueId,
        Phrase phrase,
        DialogueSpeaker speaker,
        int orderIndex,
        Instant createdAt,
        @Transient boolean isNewFlag
) implements Persistable<UUID> {

    @PersistenceCreator
    public DialoguePhrase(UUID id, UUID phraseId, Phrase phrase, DialogueSpeaker speaker,
                          int orderIndex, Instant createdAt) {
        this(id, phraseId, phrase, speaker, orderIndex, createdAt, false);
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNewFlag;
    }
}
