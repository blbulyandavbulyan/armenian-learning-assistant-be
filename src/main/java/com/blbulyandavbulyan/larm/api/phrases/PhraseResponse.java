package com.blbulyandavbulyan.larm.api.phrases;

import java.util.List;
import java.util.UUID;

import lombok.Builder;

@Builder
record PhraseResponse(
        UUID id,
        String phrase,
        String iso2LanguageCode,
        String transcription,
        List<TranslationResponse> translations) {
}
