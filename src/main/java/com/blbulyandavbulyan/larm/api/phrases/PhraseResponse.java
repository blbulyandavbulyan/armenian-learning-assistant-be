package com.blbulyandavbulyan.larm.api.phrases;

import java.util.List;
import java.util.UUID;

import lombok.Builder;

@Builder
public record PhraseResponse(
        UUID id,
        String phrase,
        String isoLanguageCode,
        String transcription,
        List<TranslationResponse> translations,
        List<Asset> assets) {

    public record Asset(String contentType, String url) {
    }
}
