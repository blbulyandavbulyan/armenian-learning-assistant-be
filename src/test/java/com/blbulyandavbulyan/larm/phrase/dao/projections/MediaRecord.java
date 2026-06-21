package com.blbulyandavbulyan.larm.phrase.dao.projections;

import java.time.Instant;
import java.util.UUID;

import com.blbulyandavbulyan.larm.phrase.dao.Media;
import com.blbulyandavbulyan.larm.phrase.dao.StorageProvider;

public record MediaRecord(
        UUID id,
        StorageProvider storageProvider,
        String storageBucket,
        String storageKey,
        String contentType,
        int sizeInBytes,
        String aiModelUsed,
        String voiceIdentifier,
        Instant createdAt) {
    public MediaRecord(Media m) {
        this(m.getId(), m.getStorageProvider(), m.getStorageBucket(),
                m.getStorageKey(), m.getContentType(), m.getSizeInBytes(),
                m.getAiModelUsed(), m.getVoiceIdentifier(), m.getCreatedAt());
    }
}
