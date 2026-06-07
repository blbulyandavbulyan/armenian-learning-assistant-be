package com.blbulyandavbulyan.larm.api.phrases;

import java.util.UUID;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Examples;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(name = "Phrase Translation")
@Builder
record TranslationResponse(
        @Schema(description = Descriptions.TRANSLATION_ID, example = Examples.TRANSLATION_ID)
        UUID id,

        @Schema(description = Descriptions.ISO_LANGUAGE_CODE, example = Examples.TRANSLATION_ISO_LANGUAGE_CODE)
        String isoLanguageCode,

        @Schema(description = Descriptions.APPROVED_TRANSLATION_TEXT, example = Examples.TRANSLATION_TEXT)
        String translationText) {
}
