package com.blbulyandavbulyan.larm.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

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