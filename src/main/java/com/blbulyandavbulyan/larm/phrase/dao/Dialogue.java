package com.blbulyandavbulyan.larm.phrase.dao;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

@Builder
@Table("dialogues")
public record Dialogue(
        @Id UUID id,
        @MappedCollection()// <- ??? what column I put here???
        Phrase title,
        Instant createdAt,
        @MappedCollection(idColumn = "dialogue_id")
        Set<DialogueSpeaker> speakers,
        @MappedCollection(idColumn = "dialogue_id")
        Set<DialoguePhrase> dialoguePhrases,
        @Transient boolean isNewFlag
) implements Persistable<UUID> {

    @PersistenceCreator
    public Dialogue(UUID id, Phrase title, Instant createdAt, Set<DialogueSpeaker> speakers,
                    Set<DialoguePhrase> dialoguePhrases) {
        this(id, title, createdAt, speakers, dialoguePhrases, false);
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
