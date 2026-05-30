package com.blbulyandavbulyan.larm.phrase;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record PhraseResource(
        UUID id,
        String phrase,
        String iso2LanguageCode,
        String transcription,
        List<TranslationResource> translations) {

}
