package com.blbulyandavbulyan.larm.phrase;

import java.util.List;
import java.util.UUID;

import lombok.Builder;

@Builder(toBuilder = true)
public record SavePhraseParameters(
        UUID id,
        String phrase,
        String isoLanguageCode,
        String transcription,
        List<CreateTranslationParameters> translations,
        List<CreateMediaResource> mediaResources) {

}
