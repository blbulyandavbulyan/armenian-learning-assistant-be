package com.blbulyandavbulyan.larm.dialogue.dao;

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
        UUID phraseId,
        UUID speakerId,
        int orderIndex,
        Instant createdAt,
        @Transient boolean isNewFlag
) implements Persistable<UUID> {

    @PersistenceCreator
    public DialoguePhrase(UUID id, UUID dialogueId, UUID phraseId, UUID speakerId,
                          int orderIndex, Instant createdAt) {
        this(id, dialogueId, phraseId, speakerId, orderIndex, createdAt, false);
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
