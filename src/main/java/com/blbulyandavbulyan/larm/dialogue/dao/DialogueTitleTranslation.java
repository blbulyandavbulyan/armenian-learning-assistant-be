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
@Table("dialogue_title_translations")
public record DialogueTitleTranslation(
        @Id UUID id,
        UUID dialogueId,
        @Column("iso_language_code") String isoLanguageCode,
        String translationText,
        Instant createdAt,
        @Transient boolean isNewFlag
) implements Persistable<UUID> {

    @PersistenceCreator
    public DialogueTitleTranslation(UUID id, UUID dialogueId, String isoLanguageCode,
                                    String translationText, Instant createdAt) {
        this(id, dialogueId, isoLanguageCode, translationText, createdAt, false);
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
