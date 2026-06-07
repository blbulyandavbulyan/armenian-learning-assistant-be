package com.blbulyandavbulyan.larm.core;

import java.util.List;

import com.blbulyandavbulyan.larm.phrase.CreateTranslationParameters;
import lombok.Builder;

@Builder
public record NewCreatePhraseParameters(
        String phrase,
        String isoLanguageCode,
        String transcription,
        List<CreateTranslationParameters> translations) {
}
