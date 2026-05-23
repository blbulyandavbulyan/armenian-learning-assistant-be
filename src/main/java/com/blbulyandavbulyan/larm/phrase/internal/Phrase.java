package com.blbulyandavbulyan.larm.phrase.internal;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@Builder
@Table("phrases")
public record Phrase(
        @Id UUID id,
        String language,
        String phrase,
        String transcription,

        @MappedCollection(idColumn = "phrase_id")
        Set<Translation> translations,
        
        @MappedCollection(idColumn = "phrase_id")
        Set<AudioFile> audioFiles,

        @Transient
        boolean isNewFlag // Used purely for Spring Data JDBC row state detection
) implements Persistable<UUID> {

    public static PhraseBuilder defaultBuilder() {
        return Phrase.builder()
                .id(UUID.randomUUID())
                .language("am")
                .audioFiles(Collections.emptySet())
                .isNewFlag(true);
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