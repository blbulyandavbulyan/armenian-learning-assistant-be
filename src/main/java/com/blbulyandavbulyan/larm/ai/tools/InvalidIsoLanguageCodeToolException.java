package com.blbulyandavbulyan.larm.ai.tools;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;


public class InvalidIsoLanguageCodeToolException extends RuntimeException {
    private final List<CreateTranslationToolParameters> invalidTranslations;

    public InvalidIsoLanguageCodeToolException(List<CreateTranslationToolParameters> invalidTranslations) {
        super("Invalid iso2 language codes for translations: %s".formatted(invalidTranslations));
        this.invalidTranslations = invalidTranslations;
    }

    @JsonPropertyDescription("List of translations for which iso2 language code was invalid")
    public List<CreateTranslationToolParameters> getInvalidTranslations() {
        return invalidTranslations;
    }
}
