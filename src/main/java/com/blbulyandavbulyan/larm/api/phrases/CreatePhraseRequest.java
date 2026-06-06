package com.blbulyandavbulyan.larm.api.phrases;

import java.util.List;

import com.blbulyandavbulyan.larm.validation.ValidIso2LanguageCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

record CreatePhraseRequest(
        @NotBlank
        String phrase,

        @NotBlank
        String transcription,

        @NotEmpty
        List<@NotNull CreateTranslationRequest> translations) {

    public record CreateTranslationRequest(
            @NotBlank
            String translationText,

            @NotBlank
            @ValidIso2LanguageCode
            String iso2LanguageCode) {
    }
}
