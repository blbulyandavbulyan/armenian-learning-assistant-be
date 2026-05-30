package com.blbulyandavbulyan.larm.api.phrases;

import lombok.Builder;

import java.util.UUID;

@Builder
record TranslationResponse(
        UUID id,
        String iso2LanguageCode,
        String translationText) {
}
