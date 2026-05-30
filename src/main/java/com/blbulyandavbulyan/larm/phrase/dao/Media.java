package com.blbulyandavbulyan.larm.phrase.dao;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
@Table("audios")
public record Media(
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

}