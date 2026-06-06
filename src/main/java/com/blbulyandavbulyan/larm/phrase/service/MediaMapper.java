package com.blbulyandavbulyan.larm.phrase.service;

import java.time.Instant;
import java.util.UUID;

import com.blbulyandavbulyan.larm.phrase.CreateMediaResource;
import com.blbulyandavbulyan.larm.phrase.MediaResource;
import com.blbulyandavbulyan.larm.phrase.dao.Media;
import org.springframework.stereotype.Component;

@Component
class MediaMapper {

    public Media toMedia(CreateMediaResource createMediaResource) {
        return Media.builder()
                .id(UUID.randomUUID())
                .phraseId(createMediaResource.phraseId())
                .storageProvider(createMediaResource.storageProvider())
                .storageBucket(createMediaResource.storageBucket())
                .storageKey(createMediaResource.storageKey())
                .contentType(createMediaResource.contentType())
                .sizeInBytes(createMediaResource.sizeInBytes())
                .aiModelUsed(createMediaResource.aiModelUsed())
                .voiceIdentifier(createMediaResource.voiceIdentifier())
                .createdAt(Instant.now())
                .isNewFlag(true)
                .build();
    }

    public MediaResource fromMedia(Media media) {
        return MediaResource.builder()
                .id(media.id())
                .phraseId(media.phraseId())
                .storageProvider(media.storageProvider())
                .storageBucket(media.storageBucket())
                .storageKey(media.storageKey())
                .contentType(media.contentType())
                .sizeInBytes(media.sizeInBytes())
                .aiModelUsed(media.aiModelUsed())
                .voiceIdentifier(media.voiceIdentifier())
                .createdAt(media.createdAt())
                .build();
    }
}
