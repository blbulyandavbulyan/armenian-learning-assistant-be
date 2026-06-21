package com.blbulyandavbulyan.larm.phrase;

import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.StorageProvider;
import lombok.Builder;

@Builder
public record CreateMediaResource(
        UUID id,
        UUID phraseId,
        StorageProvider storageProvider,
        String storageBucket,
        String storageKey,
        String contentType,
        int sizeInBytes,
        String aiModelUsed,
        String voiceIdentifier) {
}
