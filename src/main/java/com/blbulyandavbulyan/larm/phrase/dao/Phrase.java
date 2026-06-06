package com.blbulyandavbulyan.larm.phrase.dao;

import java.util.Set;
import java.util.UUID;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

@Builder
@Table("phrases")
public record Phrase(
        @Id
        UUID id,

        PhraseStatus status,

        @Column("iso_language_code")
        String isoLanguageCode,
        String phrase,
        String transcription,

        @MappedCollection(idColumn = "phrase_id")
        Set<Translation> translations,
        
        @MappedCollection(idColumn = "phrase_id")
        Set<Media> mediaSet,

        @Transient
        boolean isNewFlag // Used purely for Spring Data JDBC row state detection
) implements Persistable<UUID> {

    @PersistenceCreator
    public Phrase(UUID id, PhraseStatus status, String isoLanguageCode, String phrase,
                  String transcription, Set<Translation> translations, Set<Media> mediaSet) {
        this(id, status, isoLanguageCode, phrase, transcription, translations, mediaSet, false);
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