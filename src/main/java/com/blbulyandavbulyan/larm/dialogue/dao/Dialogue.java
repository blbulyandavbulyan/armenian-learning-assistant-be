package com.blbulyandavbulyan.larm.dialogue.dao;

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
        String title,
        String transcription,
        Instant createdAt,
        @MappedCollection(idColumn = "dialogue_id")
        Set<DialogueTitleTranslation> translations,
        @MappedCollection(idColumn = "dialogue_id")
        Set<DialoguePhrase> dialoguePhrases,
        @Transient boolean isNewFlag
) implements Persistable<UUID> {

    @PersistenceCreator
    public Dialogue(UUID id, String title, String transcription, Instant createdAt,
                    Set<DialogueTitleTranslation> translations, Set<DialoguePhrase> dialoguePhrases) {
        this(id, title, transcription, createdAt, translations, dialoguePhrases, false);
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
