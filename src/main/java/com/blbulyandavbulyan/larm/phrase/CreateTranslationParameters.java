package com.blbulyandavbulyan.larm.phrase;

import lombok.Builder;

@Builder
public record CreateTranslationParameters(String isoLanguageCode, String translationText) {
}
