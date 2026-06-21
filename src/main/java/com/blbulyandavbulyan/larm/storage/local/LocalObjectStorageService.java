package com.blbulyandavbulyan.larm.storage.local;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.blbulyandavbulyan.larm.dao.entities.StorageProvider;
import com.blbulyandavbulyan.larm.storage.ObjectStorageService;
import com.blbulyandavbulyan.larm.storage.StoredObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
class LocalObjectStorageService implements ObjectStorageService {
    private final Path storagePath;

    public LocalObjectStorageService(@Value("${storage.local.folder-name}") String storageFolder) {
        storagePath = Paths.get(storageFolder).toAbsolutePath().normalize();
    }

    @Override
    public StoredObject storeObject(byte[] bytes, String objectName) {
        Path path = storagePath.resolve(objectName).normalize();
        try {
            Files.write(path, bytes);
            return StoredObject.builder()
                    .storageKey(objectName)
                    .storageBucket(storagePath.toString())
                    .sizeInBytes(bytes.length)
                    .storageProvider(StorageProvider.LOCAL)
                    .build();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Resource loadAsResource(String storageKey) {
        Path filePath = storagePath.resolve(storageKey).normalize();
        if (!filePath.startsWith(storagePath.normalize())) {
            throw new IllegalArgumentException("Invalid storage key (path traversal attempt): " + storageKey);
        }
        if (!Files.exists(filePath)) {
            throw new RuntimeException("File not found: " + storageKey);
        }
        return new FileSystemResource(filePath);
    }
}
