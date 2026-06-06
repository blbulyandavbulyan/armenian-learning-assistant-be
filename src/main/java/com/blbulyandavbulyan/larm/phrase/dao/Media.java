package com.blbulyandavbulyan.larm.phrase.dao;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Builder
@Table("medias")
public record Media(
        @Id UUID id,
        UUID phraseId,
        StorageProvider storageProvider,
        String storageBucket,
        String storageKey,
        String contentType,
        @Column("file_size_bytes")
        int sizeInBytes,
        String aiModelUsed,
        String voiceIdentifier,
        Instant createdAt,
        @Transient // Used purely for Spring Data JDBC row state detection
        boolean isNewFlag) implements Persistable<UUID> {

    @Override
    public @Nullable UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNewFlag;
    }
}