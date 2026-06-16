package com.blbulyandavbulyan.larm.api.dialogues.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = ValidDialogueSpeakersValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDialogueSpeakers {
    String message() default "Dialogue phrases must reference defined speakers, and all defined speakers must be used";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
