package com.blbulyandavbulyan.larm.phrase;

import java.util.UUID;

import lombok.Builder;

@Builder
public record TranslationResource(UUID id, String isoLanguageCode, String translationText) {
}
