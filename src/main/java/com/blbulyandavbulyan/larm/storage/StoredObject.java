package com.blbulyandavbulyan.larm.storage;

import com.blbulyandavbulyan.larm.dao.entities.StorageProvider;
import lombok.Builder;

@Builder
public record StoredObject(
        String storageBucket,
        String storageKey,
        StorageProvider storageProvider,
        int sizeInBytes) {
}
