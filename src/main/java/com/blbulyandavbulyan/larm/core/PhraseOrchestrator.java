package com.blbulyandavbulyan.larm.core;

import com.blbulyandavbulyan.larm.ai.SpeechResource;
import com.blbulyandavbulyan.larm.ai.TextToSpeechService;
import com.blbulyandavbulyan.larm.phrase.*;
import com.blbulyandavbulyan.larm.storage.ObjectStorageService;
import com.blbulyandavbulyan.larm.storage.StoredObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PhraseOrchestrator {
    //TODO most probably we should move this guy somewhere, maybe in the phrase package

    private final IPhraseStoringService phraseStoringService;
    private final IPhraseMediaService phraseMediaService;
    private final TextToSpeechService textToSpeechService;
    private final ObjectStorageService objectStorageService;

    @Transactional //TODO dumb way, maybe we have to reconsider it
    public List<PhraseResource> savePhrases(BatchSavePhrasesParameters batchSavePhrasesParameters) {
        List<PhraseResource> phraseResources = phraseStoringService.batchSavePhrases(batchSavePhrasesParameters);


        List<CreateMediaResource> createMediaResources = new ArrayList<>();
        for (PhraseResource phraseResource : phraseResources) {
            final SpeechResource speechResource = textToSpeechService.convert(phraseResource.phrase(), phraseResource.iso2LanguageCode());
            final String objectName = phraseResource.id().toString() + UUID.randomUUID() + ".%s".formatted(speechResource.fileExtension());
            final StoredObject storedObject = objectStorageService.storeObject(speechResource.bytes(), objectName);
            createMediaResources.add(CreateMediaResource.builder()
                    .phraseId(phraseResource.id())
                    .aiModelUsed(speechResource.modelName())
                    .sizeInBytes(speechResource.sizeInBytes())
                    .storageKey(storedObject.storageKey())
                    .storageBucket(storedObject.storageBucket())
                    .storageProvider(storedObject.storageProvider())
                    .voiceIdentifier(speechResource.voiceIdentifier())
                    .contentType(speechResource.contentType())
                    .build());
        }

        List<MediaResource> mediaResources = phraseMediaService.saveMedias(createMediaResources);
        Map<UUID, List<MediaResource>> mediaResourcesByPhraseId = mediaResources.stream()
                .collect(Collectors.groupingBy(MediaResource::phraseId));
        return phraseResources.stream()
                .map(phraseResource -> phraseResource.toBuilder().media(mediaResourcesByPhraseId.get(phraseResource.id())).build())
                .toList();

    }
}
