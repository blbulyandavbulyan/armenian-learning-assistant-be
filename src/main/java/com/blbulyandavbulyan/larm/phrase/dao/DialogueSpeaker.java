package com.blbulyandavbulyan.larm.phrase.dao;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Builder
@Table("dialogue_speakers")
public record DialogueSpeaker(
        @Id UUID id,
        UUID dialogueId,
        String speakerRefId,
        @Column("name_phrase_id") UUID namePhraseId,
        Phrase namePhrase,
        Instant createdAt,
        @Transient boolean isNewFlag
) implements Persistable<UUID> {

    @PersistenceCreator
    public DialogueSpeaker(UUID id, UUID dialogueId, String speakerRefId, UUID namePhraseId, Phrase namePhrase, Instant createdAt) {
        this(id, dialogueId, speakerRefId, namePhraseId, namePhrase, createdAt, false);
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
