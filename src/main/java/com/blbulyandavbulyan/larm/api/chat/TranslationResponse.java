package com.blbulyandavbulyan.larm.api.chat;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(name = "Draft Phrase Translation Response")
@Builder
public record TranslationResponse(
        @Schema(description = OpenApiConstants.Descriptions.GENERATED_TRANSLATION_TEXT, example = OpenApiConstants.Examples.TRANSLATION_TEXT,
                requiredMode = Schema.RequiredMode.REQUIRED)
        String translationText,

        @Schema(description = OpenApiConstants.Descriptions.ISO_LANGUAGE_CODE, example = OpenApiConstants.Examples.TRANSLATION_ISO_LANGUAGE_CODE,
                requiredMode = Schema.RequiredMode.REQUIRED)
        String isoLanguageCode) {
}
