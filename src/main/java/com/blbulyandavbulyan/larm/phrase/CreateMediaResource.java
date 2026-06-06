package com.blbulyandavbulyan.larm.phrase;

import java.util.UUID;

import com.blbulyandavbulyan.larm.phrase.dao.StorageProvider;
import lombok.Builder;

@Builder
public record CreateMediaResource(
        UUID phraseId,
        StorageProvider storageProvider,
        String storageBucket,
        String storageKey,
        String contentType,
        int sizeInBytes,
        String aiModelUsed,
        String voiceIdentifier) {
}
