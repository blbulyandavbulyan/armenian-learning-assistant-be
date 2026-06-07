package com.blbulyandavbulyan.larm.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.blbulyandavbulyan.larm.ai.SpeechResource;
import com.blbulyandavbulyan.larm.ai.TextToSpeechService;
import com.blbulyandavbulyan.larm.phrase.BatchSavePhrasesParameters;
import com.blbulyandavbulyan.larm.phrase.CreateMediaResource;
import com.blbulyandavbulyan.larm.phrase.MediaResource;
import com.blbulyandavbulyan.larm.phrase.PhraseMediaService;
import com.blbulyandavbulyan.larm.phrase.PhraseResource;
import com.blbulyandavbulyan.larm.phrase.PhraseStoringService;
import com.blbulyandavbulyan.larm.storage.ObjectStorageService;
import com.blbulyandavbulyan.larm.storage.StoredObject;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PhraseOrchestrator {
    // TODO most probably we should move this guy somewhere, maybe in the phrase package

    private final PhraseStoringService phraseStoringService;
    private final PhraseMediaService phraseMediaService;
    private final TextToSpeechService textToSpeechService;
    private final ObjectStorageService objectStorageService;

    @Transactional // TODO dumb way, maybe we have to reconsider it
    @Timed(value = "save.phrases.total-time", description = "Includes total time for saving phrases including additional stuff like text-to-speach")
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
