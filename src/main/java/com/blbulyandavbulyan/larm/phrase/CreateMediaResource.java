package com.blbulyandavbulyan.larm.phrase;

import com.blbulyandavbulyan.larm.dao.entities.StorageProvider;
import lombok.Builder;

@Builder
public record CreateMediaResource(
        StorageProvider storageProvider,
        String storageBucket,
        String storageKey,
        String contentType,
        int sizeInBytes,
        String aiModelUsed,
        String voiceIdentifier) {
}
