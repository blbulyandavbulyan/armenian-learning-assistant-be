package com.blbulyandavbulyan.larm.validation;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class IsoLanguageCodeValidatorBridge implements ConstraintValidator<ValidIsoLanguageCode, String> {

    private static final String[] EMPTY_STRING_ARRAY = {};
    private final IsoLanguageValidator isoLanguageValidator;
    private List<String> supportedLanguages;

    @Autowired
    public IsoLanguageCodeValidatorBridge(IsoLanguageValidator isoLanguageValidator) {
        this.isoLanguageValidator = isoLanguageValidator;
    }

    @Override
    public void initialize(ValidIsoLanguageCode constraintAnnotation) {
        String[] languages = Optional.ofNullable(constraintAnnotation.supportedLanguages())
                .orElse(EMPTY_STRING_ARRAY);

        // Validate the annotation configuration itself on startup!
        for (String lang : languages) {
            if (isoLanguageValidator.isNotValid(lang)) {
                throw new IllegalArgumentException(
                        "Invalid configuration in @ValidIsoLanguageCode: '%s' is not a valid ISO language code.".formatted(lang));
            }
        }

        this.supportedLanguages = Arrays.asList(languages);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Good practice: let @NotNull or @NotBlank handle null checks if needed, 
        // or return false if you want it to always fail on null.
        if (value == null) {
            return true; 
        }

        if (!supportedLanguages.isEmpty() && !supportedLanguages.contains(value)) {
            return false;
        }

        // Reusing your existing logic (negating isNotValid)
        return !isoLanguageValidator.isNotValid(value);
    }
}