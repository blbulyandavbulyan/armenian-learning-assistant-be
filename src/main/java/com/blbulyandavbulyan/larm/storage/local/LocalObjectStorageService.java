package com.blbulyandavbulyan.larm.storage.local;

import com.blbulyandavbulyan.larm.storage.StoredObject;
import com.blbulyandavbulyan.larm.phrase.dao.StorageProvider;
import com.blbulyandavbulyan.larm.storage.ObjectStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
class LocalObjectStorageService implements ObjectStorageService {
    @Value("storage.local.folder-name")
    private final String storageFolder;

    @Override
    public StoredObject storeObject(byte[] bytes, String objectName) {
        Path path = Paths.get(storageFolder).resolve(objectName);
        try {
            Files.write(path, bytes);
            return StoredObject.builder()
                    .storageKey(objectName)
                    .storageBucket(storageFolder)
                    .sizeInBytes(bytes.length)
                    .storageProvider(StorageProvider.LOCAL)
                    .build();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
