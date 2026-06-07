package com.blbulyandavbulyan.larm.api.phrases;

import java.util.List;

import com.blbulyandavbulyan.larm.core.NewCreatePhraseParameters;
import com.blbulyandavbulyan.larm.phrase.CreateTranslationParameters;
import org.springframework.stereotype.Component;

@Component
class PhraseRequestMapper {
    public List<NewCreatePhraseParameters> mapToBatchPhrasesParameters(CreatePhrasesRequest request) {
        return request.phrases()
                .stream()
                .map(PhraseRequestMapper::mapToSavePhraseParameters)
                .toList();
    }

    private static NewCreatePhraseParameters mapToSavePhraseParameters(CreatePhraseRequest request) {
        return NewCreatePhraseParameters.builder()
                .phrase(request.phrase())
                .transcription(request.transcription())
                .isoLanguageCode("hy")
                .translations(mapTranslations(request.translations()))
                .build();
    }

    private static List<CreateTranslationParameters> mapTranslations(List<CreatePhraseRequest.CreateTranslationRequest> translations) {
        return translations.stream().map(PhraseRequestMapper::mapTranslation).toList();
    }

    private static CreateTranslationParameters mapTranslation(CreatePhraseRequest.CreateTranslationRequest translation) {
        return CreateTranslationParameters.builder()
                .translationText(translation.translationText())
                .isoLanguageCode(translation.isoLanguageCode())
                .build();
    }
}
