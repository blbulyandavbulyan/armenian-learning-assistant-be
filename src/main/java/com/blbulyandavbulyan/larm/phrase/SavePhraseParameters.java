package com.blbulyandavbulyan.larm.phrase;

import java.util.List;
import java.util.UUID;

import lombok.Builder;

@Builder(toBuilder = true)
// TODO sonar warning java:S6218
public record SavePhraseParameters(
        UUID id,
        String phrase,
        String isoLanguageCode,
        String transcription,
        float[] embedding,
        List<CreateTranslationParameters> translations,
        List<CreateMediaResource> mediaResources) {

}
