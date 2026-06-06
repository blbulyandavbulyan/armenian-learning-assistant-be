package com.blbulyandavbulyan.larm.phrase;

import java.util.List;
import java.util.UUID;

import lombok.Builder;

@Builder(toBuilder = true)
public record PhraseResource(
        UUID id,
        String phrase,
        String iso2LanguageCode,
        String transcription,
        List<TranslationResource> translations,
        List<MediaResource> media) {

}
