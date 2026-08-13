package com.blbulyandavbulyan.larm.storage;

import org.springframework.core.io.Resource;

public interface ObjectStorageService {
    StoredObject storeObject(byte[] bytes, String objectName);

    /**
     * Loads stored object as {@link Resource}.
     *
     * @param storageKey to fetch object
     * @return loaded object as {@link Resource}
     * @throws ObjectNotFoundException if object is not found
     */
    Resource loadAsResource(String storageKey);
}
