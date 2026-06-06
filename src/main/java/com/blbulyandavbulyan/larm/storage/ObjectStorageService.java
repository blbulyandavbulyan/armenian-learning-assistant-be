package com.blbulyandavbulyan.larm.storage;

import org.springframework.core.io.Resource;

public interface ObjectStorageService {
    StoredObject storeObject(byte[] bytes, String objectName);

    Resource loadAsResource(String storageKey);
}
