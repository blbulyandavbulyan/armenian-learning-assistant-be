package com.blbulyandavbulyan.larm.phrase;

import com.blbulyandavbulyan.larm.phrase.dao.StorageProvider;
import lombok.Builder;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record MediaResource(
        @Id UUID id,
        UUID phraseId,
        StorageProvider storageProvider,
        String storageBucket,
        String storageKey,
        String contentType,
        int sizeInBytes,
        String aiModelUsed,
        String voiceIdentifier,
        Instant createdAt) {
}
