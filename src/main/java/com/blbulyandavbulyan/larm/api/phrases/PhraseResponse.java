package com.blbulyandavbulyan.larm.api.phrases;

import java.util.List;
import java.util.UUID;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Examples;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(name = "Phrase Response")
@Builder
public record PhraseResponse(
        @Schema(description = Descriptions.PHRASE_ID, example = Examples.PHRASE_ID, requiredMode = Schema.RequiredMode.REQUIRED)
        UUID id,

        @Schema(description = Descriptions.APPROVED_PHRASE, example = Examples.PHRASE, requiredMode = Schema.RequiredMode.REQUIRED)
        String phrase,

        @Schema(description = Descriptions.ISO_LANGUAGE_CODE, example = Examples.PHRASE_ISO_LANGUAGE_CODE, requiredMode = Schema.RequiredMode.REQUIRED)
        String isoLanguageCode,

        @Schema(description = Descriptions.APPROVED_TRANSCRIPTION, example = Examples.TRANSCRIPTION, requiredMode = Schema.RequiredMode.REQUIRED)
        String transcription,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<TranslationResponse> translations,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<Asset> assets) {

    @Schema(name = "Asset Of Phrase Response")
    public record Asset(
            @Schema(description = Descriptions.CONTENT_TYPE, example = Examples.CONTENT_TYPE, requiredMode = Schema.RequiredMode.REQUIRED)
            String contentType,

            @Schema(description = Descriptions.ASSET_URL, example = Examples.ASSET_URL, requiredMode = Schema.RequiredMode.REQUIRED)
            String url) {
    }
}
