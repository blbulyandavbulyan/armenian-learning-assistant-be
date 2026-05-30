package com.blbulyandavbulyan.larm.ai.tools;

import lombok.Builder;

@Builder
public record CreateTranslationToolParameters(String isoLanguageCode, String translationText) {
}
