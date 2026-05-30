package com.blbulyandavbulyan.larm.phrase.service;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class IsoLanguageValidator {
    private static final Set<String> ISO_LANGUAGES = Arrays.stream(Locale.getISOLanguages()).collect(Collectors.toSet());

    public boolean isValid(String languageCode) {
        return ISO_LANGUAGES.contains(languageCode);
    }

}
