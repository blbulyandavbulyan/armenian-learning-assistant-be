package com.blbulyandavbulyan.larm.validation.validators;

import com.blbulyandavbulyan.larm.BaseIT;
import com.blbulyandavbulyan.larm.validation.ValidIsoLanguageCode;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IsoLanguageCodeValidatorIT extends BaseIT {

    @Autowired
    private Validator validator;

    record TestRecordNullValue(
            @ValidIsoLanguageCode
            String languageCode) {
    }

    @Test
    void shouldBeValid_whenValueIsNull() {
        var testRecord = new TestRecordNullValue(null);

        var violations = validator.validate(testRecord);

        assertThat(violations).isEmpty();
    }

    record TestRecordInvalidAnnotation(
            @ValidIsoLanguageCode(supportedLanguages = {"invalid-lang"})
            String languageCode) {
    }

    @Test
    void shouldThrowIllegalArgumentException_whenAnnotationHasInvalidLanguage() {
        var testRecord = new TestRecordInvalidAnnotation("en");

        assertThatThrownBy(() -> validator.validate(testRecord))
                .isInstanceOf(ValidationException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class)
                .hasStackTraceContaining("Invalid configuration in @ValidIsoLanguageCode: 'invalid-lang' is not a valid ISO language code.");
    }
}
