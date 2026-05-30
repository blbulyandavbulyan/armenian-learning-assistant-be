package com.blbulyandavbulyan.larm.api.phrases;

import java.util.List;

record CreatePhraseRequest(
        String phrase,
        String transcription,
        List<CreateTranslationRequest> translations) {

    public record CreateTranslationRequest(String translationText, String iso2LanguageCode) {
    }
}
