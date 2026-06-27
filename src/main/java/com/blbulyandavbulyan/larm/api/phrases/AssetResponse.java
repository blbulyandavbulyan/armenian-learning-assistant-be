package com.blbulyandavbulyan.larm.api.phrases;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(name = "Asset Response")
@Builder
public record AssetResponse(
        @Schema(description = OpenApiConstants.Descriptions.CONTENT_TYPE, example = OpenApiConstants.Examples.CONTENT_TYPE,
                requiredMode = Schema.RequiredMode.REQUIRED)
        String contentType,

        @Schema(description = OpenApiConstants.Descriptions.ASSET_URL, example = OpenApiConstants.Examples.ASSET_URL,
                requiredMode = Schema.RequiredMode.REQUIRED)
        String url) {
}
