package com.blbulyandavbulyan.larm.storage;

public interface ObjectStorageService {
    StoredObject storeObject(byte[] bytes, String objectName);
}
