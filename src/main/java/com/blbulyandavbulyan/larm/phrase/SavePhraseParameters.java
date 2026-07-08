package com.blbulyandavbulyan.larm.phrase;

import java.util.List;

import lombok.Builder;

@Builder(toBuilder = true)
public record SavePhraseParameters(
        String phrase,
        String isoLanguageCode,
        String transcription,
        List<CreateTranslationParameters> translations,
        List<CreateMediaResource> mediaResources) {

}
