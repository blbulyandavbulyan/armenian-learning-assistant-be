package com.blbulyandavbulyan.larm.ai.tools;

import com.blbulyandavbulyan.larm.ai.common.ScopedValues;
import com.blbulyandavbulyan.larm.phrase.CreateTranslationParameters;
import com.blbulyandavbulyan.larm.phrase.IPhraseService;
import com.blbulyandavbulyan.larm.phrase.SavePhraseParameters;
import com.blbulyandavbulyan.larm.phrase.service.BatchSavePhrasesParameters;
import com.blbulyandavbulyan.larm.phrase.service.BatchSavePhrasesResult;
import com.blbulyandavbulyan.larm.phrase.service.InvalidIsoLanguageCodeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SavePhrasesTool {
    private final IPhraseService phraseService;

    @Tool(name = "batch_save_phrases", description = "Saves phrases in batch")
    public BatchSavePhraseToolResult batchSavePhrases(BatchSavePhrasesToolParameters batchSavePhrasesToolParameters) {
//        TODO, such workflow is not reliable enough to use in production
//         when AI isn't prompted to not call the tool unless approved -> it calls immediately, when it is prompted so -> it could skip the tool call
//         so we have to redesign workflow, only structured response should be left, and maybe some other tool which checks if the phrase already exists in the db
//         and then, separate api endpoint should be created, like POST /phrases -> and this endpoint should be responsible for saving the phrases, generating embeddings, generating audio and so on
        log.debug("Tool was called with the following parameters: {}, for chat id: {}", batchSavePhrasesToolParameters, ScopedValues.CONVERSATION_ID.get());
        try {
            //TODO we only saved here phrase to the database
            // Next steps:
            // 1. Populate embeddings
            // 2. Reach to eleven labs API to get the audio for that phrase
            // 3. Save and attach the audio to the phrase
            BatchSavePhrasesResult batchSavePhrasesResult = phraseService.batchSavePhrases(new BatchSavePhrasesParameters(
                    batchSavePhrasesToolParameters.createPhrases()
                            .stream()
                            .map(this::mapToSavePhraseParameters)
                            .toList()));
            return mapToToolResult(batchSavePhrasesResult);
        } catch (InvalidIsoLanguageCodeException e) {
            log.error("Error occurred while calling tool", e);
            throw new InvalidIsoLanguageCodeToolException(e.getInvalidTranslations().stream()
                    .map(SavePhrasesTool::fromTranslationParametersToToolParameters).toList());
        }
    }

    private static CreateTranslationToolParameters fromTranslationParametersToToolParameters(CreateTranslationParameters t) {
        return CreateTranslationToolParameters.builder()
                .isoLanguageCode(t.isoLanguageCode())
                .translationText(t.translationText())
                .build();
    }

    private SavePhraseParameters mapToSavePhraseParameters(CreatePhraseToolParameters createPhraseToolParameters) {
        return SavePhraseParameters.builder()
                .phrase(createPhraseToolParameters.phrase())
                .transcription(createPhraseToolParameters.transcription())
                .isoLanguageCode("am")
                .translations(mapTranslations(createPhraseToolParameters))
                .build();

    }

    private static List<CreateTranslationParameters> mapTranslations(CreatePhraseToolParameters createPhraseToolParameters) {
        return createPhraseToolParameters.translations().stream().map(t -> CreateTranslationParameters.builder().translationText(t.translationText()).isoLanguageCode(t.isoLanguageCode()).build()).toList();
    }

    private BatchSavePhraseToolResult mapToToolResult(BatchSavePhrasesResult batchSavePhrasesResult) {
        return BatchSavePhraseToolResult.builder()
                .existingPhrases(batchSavePhrasesResult.existingPhrases())
                .savedPhrases(mapToPhraseSaveResultList(batchSavePhrasesResult))
                .build();
    }

    private static List<BatchSavePhraseToolResult.PhraseSaveResult> mapToPhraseSaveResultList(BatchSavePhrasesResult batchSavePhrasesResult) {
        return batchSavePhrasesResult.savedPhrases().stream()
                .map(p -> BatchSavePhraseToolResult.PhraseSaveResult.builder()
                        .id(p.id())
                        .phrase(p.phrase())
                        .build())
                .toList();
    }
}
