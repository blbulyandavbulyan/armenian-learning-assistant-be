package com.blbulyandavbulyan.larm.ai.embedding.util;

import java.util.List;
import java.util.stream.Collectors;

import com.blbulyandavbulyan.larm.phrase.CreateTranslationParameters;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TranslationUtils {
    public static String extractTranslations(List<CreateTranslationParameters> translations) {
        return translations.stream()
                .map(CreateTranslationParameters::translationText)
                .collect(Collectors.joining(", "));
    }
}
