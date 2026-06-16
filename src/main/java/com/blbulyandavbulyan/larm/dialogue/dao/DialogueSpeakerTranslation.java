package com.blbulyandavbulyan.larm.dialogue.dao;

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
@Table("dialogue_speaker_translations")
public record DialogueSpeakerTranslation(
        @Id UUID id,
        UUID speakerId,
        @Column("iso_language_code") String isoLanguageCode,
        String translationText,
        Instant createdAt,
        @Transient boolean isNewFlag
) implements Persistable<UUID> {

    @PersistenceCreator
    public DialogueSpeakerTranslation(UUID id, UUID speakerId, String isoLanguageCode,
                                      String translationText, Instant createdAt) {
        this(id, speakerId, isoLanguageCode, translationText, createdAt, false);
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
