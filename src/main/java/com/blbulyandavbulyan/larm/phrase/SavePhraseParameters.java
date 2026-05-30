package com.blbulyandavbulyan.larm.phrase;

import lombok.Builder;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@Builder
public record SavePhraseParameters(
        String phrase,
        String isoLanguageCode,
        String transcription,
        List<CreateTranslationParameters> translations) {

}
