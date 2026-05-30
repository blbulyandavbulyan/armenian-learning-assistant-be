package com.blbulyandavbulyan.larm.phrase;

import lombok.Builder;

import java.util.UUID;

@Builder
public record TranslationResource(UUID id, String iso2LanguageCode, String translationText) {
}
