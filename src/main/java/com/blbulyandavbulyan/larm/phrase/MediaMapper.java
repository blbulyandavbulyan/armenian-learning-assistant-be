package com.blbulyandavbulyan.larm.phrase;

import java.time.Instant;

import com.blbulyandavbulyan.larm.dao.entities.Media;
import org.springframework.stereotype.Component;

@Component
class MediaMapper {

    public Media toMedia(CreateMediaResource createMediaResource) {
        return Media.builder()
                .storageProvider(createMediaResource.storageProvider())
                .storageBucket(createMediaResource.storageBucket())
                .storageKey(createMediaResource.storageKey())
                .contentType(createMediaResource.contentType())
                .sizeInBytes(createMediaResource.sizeInBytes())
                .aiModelUsed(createMediaResource.aiModelUsed())
                .voiceIdentifier(createMediaResource.voiceIdentifier())
                .createdAt(Instant.now())
                .build();
    }
}
