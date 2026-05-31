package com.blbulyandavbulyan.larm.storage;

import com.blbulyandavbulyan.larm.phrase.dao.StorageProvider;
import lombok.Builder;

@Builder
public record StoredObject(
        String storageBucket,
        String storageKey,
        StorageProvider storageProvider,
        int sizeInBytes) {
}
