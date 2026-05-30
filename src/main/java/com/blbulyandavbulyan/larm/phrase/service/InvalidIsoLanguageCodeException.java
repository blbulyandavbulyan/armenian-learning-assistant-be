package com.blbulyandavbulyan.larm.phrase.service;

import com.blbulyandavbulyan.larm.phrase.CreateTranslationParameters;
import lombok.Getter;

import java.util.List;

@Getter
public class InvalidIsoLanguageCodeException extends IllegalArgumentException {
    private final List<CreateTranslationParameters> invalidTranslations;

    public InvalidIsoLanguageCodeException(List<CreateTranslationParameters> invalidTranslations) {
        this.invalidTranslations = invalidTranslations;
        super("Invalid iso language codes for translations: %s".formatted(invalidTranslations));
    }
}
