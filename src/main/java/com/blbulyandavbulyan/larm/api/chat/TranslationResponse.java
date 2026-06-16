package com.blbulyandavbulyan.larm.api.chat;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(name = "Chat Translation Response")
@Builder
public record TranslationResponse(
        @Schema(description = Descriptions.GENERATED_TRANSLATION_TEXT,
                requiredMode = Schema.RequiredMode.REQUIRED)
        String translationText,

        @Schema(description = Descriptions.ISO_LANGUAGE_CODE,
                requiredMode = Schema.RequiredMode.REQUIRED)
        String isoLanguageCode) {
}
