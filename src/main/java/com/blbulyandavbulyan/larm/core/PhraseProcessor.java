package com.blbulyandavbulyan.larm.core;

import java.util.List;
import java.util.UUID;

import com.blbulyandavbulyan.larm.ai.SpeechResource;
import com.blbulyandavbulyan.larm.ai.TextToSpeechService;
import com.blbulyandavbulyan.larm.phrase.CreateMediaResource;
import com.blbulyandavbulyan.larm.phrase.SavePhraseParameters;
import com.blbulyandavbulyan.larm.storage.ObjectStorageService;
import com.blbulyandavbulyan.larm.storage.StoredObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PhraseProcessor {
    private final TextToSpeechService textToSpeechService;
    private final ObjectStorageService objectStorageService;

    /**
     * Generates TTS audio for a phrase, stores it, and returns the phrase parameters
     * ready to be persisted.
     */
    public SavePhraseParameters process(CreateNewPhraseParameters parameters) {
        final var phraseId = UUID.randomUUID();
        final var mediaId = UUID.randomUUID();
        final SpeechResource speechResource = textToSpeechService.convert(parameters.phrase(), parameters.isoLanguageCode());
        final var objectName = phraseId.toString() + mediaId + ".%s".formatted(speechResource.fileExtension());
        final StoredObject storedObject = objectStorageService.storeObject(speechResource.bytes(), objectName);
        final var mediaResource = CreateMediaResource.builder()
                .id(mediaId)
                .phraseId(phraseId)
                .aiModelUsed(speechResource.modelName())
                .sizeInBytes(speechResource.sizeInBytes())
                .storageKey(storedObject.storageKey())
                .storageBucket(storedObject.storageBucket())
                .storageProvider(storedObject.storageProvider())
                .voiceIdentifier(speechResource.voiceIdentifier())
                .contentType(speechResource.contentType())
                .build();

        return SavePhraseParameters.builder()
                .id(phraseId)
                .phrase(parameters.phrase())
                .transcription(parameters.transcription())
                .isoLanguageCode(parameters.isoLanguageCode())
                .translations(parameters.translations())
                .mediaResources(List.of(mediaResource))
                .build();
    }
}

