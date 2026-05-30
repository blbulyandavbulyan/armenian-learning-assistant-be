package com.blbulyandavbulyan.larm.api.phrases;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record PhraseResponse(
        UUID id,
        String phrase,
        String iso2LanguageCode,
        String transcription,
        List<TranslationResponse> translations) {
}
