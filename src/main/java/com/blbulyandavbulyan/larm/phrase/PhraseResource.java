package com.blbulyandavbulyan.larm.phrase;

import java.util.List;
import java.util.UUID;

import lombok.Builder;

@Builder(toBuilder = true)
public record PhraseResource(
        UUID id,
        String phrase,
        String isoLanguageCode,
        String transcription,
        List<TranslationResource> translations,
        List<MediaResource> media) {

}
