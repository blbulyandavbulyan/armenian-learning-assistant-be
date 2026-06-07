package com.blbulyandavbulyan.larm.api.phrases;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Examples;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(name = "Page Response")
@Builder
record PageResponse(

        @Schema(description = Descriptions.PAGE_NUMBER, defaultValue = "1",
                example = Examples.PAGE_NUMBER, requiredMode = Schema.RequiredMode.REQUIRED)
        int pageNumber,

        @Schema(description = Descriptions.PAGE_SIZE, defaultValue = "10",
                example = Examples.PAGE_SIZE, requiredMode = Schema.RequiredMode.REQUIRED)
        int pageSize,

        @Schema(description = Descriptions.TOTAL_PAGES, example = Examples.TOTAL_PAGES, requiredMode = Schema.RequiredMode.REQUIRED)
        int totalPages) {
}
