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
@Table("dialogue_speakers")
public record DialogueSpeaker(
        @Id UUID id,
        String speakerRefId,
        String title,
        String transcription,
        Instant createdAt,
        @MappedCollection(idColumn = "speaker_id")
        Set<DialogueSpeakerTranslation> translations,
        @Transient boolean isNewFlag
) implements Persistable<UUID> {

    @PersistenceCreator
    public DialogueSpeaker(UUID id, String speakerRefId, String title, String transcription,
                           Instant createdAt, Set<DialogueSpeakerTranslation> translations) {
        this(id, speakerRefId, title, transcription, createdAt, translations, false);
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
