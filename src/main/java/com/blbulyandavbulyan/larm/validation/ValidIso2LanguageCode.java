package com.blbulyandavbulyan.larm.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = Iso2LanguageCodeValidatorBridge.class)
@Target({ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidIso2LanguageCode {
    
    // The default error message when validation fails
    String message() default "Invalid ISO-2 language code";

    // Required by the Jakarta Validation spec
    Class<?>[] groups() default {};

    // Required by the Jakarta Validation spec
    Class<? extends Payload>[] payload() default {};
}