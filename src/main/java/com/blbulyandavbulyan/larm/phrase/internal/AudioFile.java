package com.blbulyandavbulyan.larm.phrase.internal;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
@Table("audio_files")
public record AudioFile(
        @Id UUID id,
        StorageProvider storageProvider,
        String storageBucket,
        String storageKey,
        String contentType,
        Long fileSizeBytes,
        BigDecimal durationSeconds,
        String aiModelUsed,
        String voiceIdentifier,
        Instant createdAt) {

    public static AudioFileBuilder defaultBuilderForLocal() {
        return builder()
                .id(UUID.randomUUID())
                .storageProvider(StorageProvider.LOCAL)
                .createdAt(Instant.now());
    }
}