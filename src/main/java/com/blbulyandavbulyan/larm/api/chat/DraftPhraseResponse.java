package com.blbulyandavbulyan.larm.api.chat;

import lombok.Builder;

import java.util.List;

@Builder
public record DraftPhraseResponse(
        String phrase,
        String transcription,
        List<TranslationResponse> translations) {

    @Builder
    public record TranslationResponse(String translationText, String iso2LanguageCode) {
    }
}
