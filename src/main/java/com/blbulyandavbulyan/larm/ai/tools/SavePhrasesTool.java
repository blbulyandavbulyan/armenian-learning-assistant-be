package com.blbulyandavbulyan.larm.ai.tools;

import com.blbulyandavbulyan.larm.phrase.CreateTranslationParameters;
import com.blbulyandavbulyan.larm.phrase.IPhraseService;
import com.blbulyandavbulyan.larm.phrase.SavePhraseParameters;
import com.blbulyandavbulyan.larm.phrase.service.BatchSavePhrasesParameters;
import com.blbulyandavbulyan.larm.phrase.service.BatchSavePhrasesResult;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SavePhrasesTool {
    private final IPhraseService phraseService;

    @Tool(name = "batch_save_phrases", description = "Saves phrases in batch")
    public BatchSavePhraseToolResult batchSavePhrases(BatchSavePhrasesToolParameters batchSavePhrasesToolParameters) {

        BatchSavePhrasesResult batchSavePhrasesResult = phraseService.batchSavePhrases(new BatchSavePhrasesParameters(
                batchSavePhrasesToolParameters.createPhrases()
                        .stream()
                        .map(this::mapToSavePhraseParameters)
                        .toList()));
        return mapToToolResult(batchSavePhrasesResult);
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
