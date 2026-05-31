package com.blbulyandavbulyan.larm.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class Iso2LanguageCodeValidatorBridge implements ConstraintValidator<ValidIso2LanguageCode, String> {

    private final IsoLanguageValidator isoLanguageValidator;

    // Spring will automatically inject your component here
    @Autowired
    public Iso2LanguageCodeValidatorBridge(IsoLanguageValidator isoLanguageValidator) {
        this.isoLanguageValidator = isoLanguageValidator;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Good practice: let @NotNull or @NotBlank handle null checks if needed, 
        // or return false if you want it to always fail on null.
        if (value == null) {
            return true; 
        }

        // Reusing your existing logic (negating isNotValid)
        return !isoLanguageValidator.isNotValid(value);
    }
}