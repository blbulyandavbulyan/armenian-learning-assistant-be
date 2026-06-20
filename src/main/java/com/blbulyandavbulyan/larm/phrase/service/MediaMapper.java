package com.blbulyandavbulyan.larm.phrase.service;

import java.time.Instant;

import com.blbulyandavbulyan.larm.phrase.CreateMediaResource;
import com.blbulyandavbulyan.larm.phrase.dao.Media;
import org.springframework.stereotype.Component;

@Component
class MediaMapper {

    public Media toMedia(CreateMediaResource createMediaResource) {
        return Media.builder()
                .id(createMediaResource.id())
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
