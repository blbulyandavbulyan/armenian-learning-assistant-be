package com.blbulyandavbulyan.larm.phrase;

import java.util.Collections;
import java.util.List;

import lombok.Getter;

@Getter
public class InvalidIsoLanguageCodeException extends IllegalArgumentException {
    /**
     * Translations with invalid iso language code.
     */
    private final List<CreateTranslationParameters> invalidTranslations;

    /**
     * Phrases with invalid iso language code.
     */
    private final List<SavePhraseParameters> invalidPhrases;

    public InvalidIsoLanguageCodeException(List<CreateTranslationParameters> invalidTranslations, List<SavePhraseParameters> invalidPhrases) {
        this.invalidTranslations = Collections.unmodifiableList(invalidTranslations);
        this.invalidPhrases = Collections.unmodifiableList(invalidPhrases);
        super("Invalid iso language codes for translations: %s".formatted(invalidTranslations));
    }
}
