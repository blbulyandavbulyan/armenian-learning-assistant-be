package com.blbulyandavbulyan.larm.api.phrases;

import com.blbulyandavbulyan.larm.phrase.CreateTranslationParameters;
import com.blbulyandavbulyan.larm.phrase.SavePhraseParameters;
import com.blbulyandavbulyan.larm.phrase.BatchSavePhrasesParameters;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class PhraseRequestMapper {
    public BatchSavePhrasesParameters mapToBatchPhrasesParameters(CreatePhrasesRequest request) {
        List<SavePhraseParameters> phraseResources = request.phrases()
                .stream()
                .map(PhraseRequestMapper::mapToSavePhraseParameters)
                .toList();
        return new BatchSavePhrasesParameters(phraseResources);
    }

    private static SavePhraseParameters mapToSavePhraseParameters(CreatePhraseRequest request) {
        return SavePhraseParameters.builder()
                .phrase(request.phrase())
                .transcription(request.transcription())
                .isoLanguageCode("am")
                .translations(mapTranslations(request.translations()))
                .build();
    }

    private static List<CreateTranslationParameters> mapTranslations(List<CreatePhraseRequest.CreateTranslationRequest> translations) {
        return translations.stream().map(PhraseRequestMapper::mapTranslation).toList();
    }

    private static CreateTranslationParameters mapTranslation(CreatePhraseRequest.CreateTranslationRequest translation) {
        return CreateTranslationParameters.builder()
                .translationText(translation.translationText())
                .isoLanguageCode(translation.iso2LanguageCode())
                .build();
    }
}
