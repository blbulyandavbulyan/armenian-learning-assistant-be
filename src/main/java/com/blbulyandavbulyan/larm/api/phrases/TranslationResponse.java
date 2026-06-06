package com.blbulyandavbulyan.larm.api.phrases;

import java.util.UUID;

import lombok.Builder;

@Builder
record TranslationResponse(
        UUID id,
        String iso2LanguageCode,
        String translationText) {
}
