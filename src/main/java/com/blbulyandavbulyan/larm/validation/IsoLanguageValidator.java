package com.blbulyandavbulyan.larm.validation;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class IsoLanguageValidator {
    private static final Set<String> ISO_LANGUAGES = Arrays.stream(Locale.getISOLanguages()).collect(Collectors.toSet());

    public boolean isNotValid(String languageCode) {
        return !ISO_LANGUAGES.contains(languageCode);
    }

}
