package com.blbulyandavbulyan.larm.core;

import java.util.List;
import java.util.UUID;

import com.blbulyandavbulyan.larm.ai.SpeechResource;
import com.blbulyandavbulyan.larm.ai.TextToSpeechService;
import com.blbulyandavbulyan.larm.phrase.BatchSavePhrasesParameters;
import com.blbulyandavbulyan.larm.phrase.CreateMediaResource;
import com.blbulyandavbulyan.larm.phrase.PhraseResource;
import com.blbulyandavbulyan.larm.phrase.PhraseStoringService;
import com.blbulyandavbulyan.larm.phrase.SavePhraseParameters;
import com.blbulyandavbulyan.larm.storage.ObjectStorageService;
import com.blbulyandavbulyan.larm.storage.StoredObject;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PhraseOrchestrator {
    // TODO most probably we should move this guy somewhere, maybe in the phrase package

    private final PhraseStoringService phraseStoringService;
    private final TextToSpeechService textToSpeechService;
    private final ObjectStorageService objectStorageService;

    @Timed(value = "save.phrases.total-time", description = "Includes total time for saving phrases including additional stuff like text-to-speach")
    public List<PhraseResource> savePhrases(List<NewCreatePhraseParameters> newCreatePhraseParameters) {
        final var phrasesWithMedias = initialProcess(newCreatePhraseParameters);
        return phraseStoringService.batchSavePhrases(phrasesWithMedias);
    }

    private BatchSavePhrasesParameters initialProcess(List<NewCreatePhraseParameters> newCreatePhraseParameters) {
        return new BatchSavePhrasesParameters(newCreatePhraseParameters.stream().parallel().map(this::processPhrase).toList());
    }

    private SavePhraseParameters processPhrase(NewCreatePhraseParameters parameters) {
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
