package com.blbulyandavbulyan.larm.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = IsoLanguageCodeValidatorBridge.class)
@Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidIsoLanguageCode {
    
    // The default error message when validation fails
    String message() default "Invalid ISO 639-1 language code";

    // Required by the Jakarta Validation spec
    Class<?>[] groups() default {};

    // Required by the Jakarta Validation spec
    Class<? extends Payload>[] payload() default {};

    /**
     *
     * @return supported languages, if empty all languages are supported
     */
    String[] supportedLanguages() default {};
}